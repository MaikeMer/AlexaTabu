/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
        http://aws.amazon.com/apache2.0/
    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.amazon.customskill;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.customskill.AlexaSkillSpeechlet.UserIntent;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;

import java.lang.Math;

/*
 * This class is the actual skill. Here you receive the input and have to produce the speech output. 
 */
public class AlexaSkillSpeechlet
implements SpeechletV2
{
	static Logger logger = LoggerFactory.getLogger(AlexaSkillSpeechlet.class);
	
	//Idee: Variablen die wir auch in Dialogos benutzt haben z.B. e1,e2,e3
	static String tabuwort = ""; 
	static String explain = "";
	
    //Was User sagt 
	public static String userRequest;
	
	//Muss noch angepasst werden auf unser Modell
	//M�chten Sie das Spiel anfangen oder weiterspielen? - Ja - selectTabuwort
	//Nein - Aufwiedersehen, danke f�rs spielen
	static enum RecognitionState {Erklaerung, JaNein};
	RecognitionState recState;
	
	//Was User gerade gesagt hat - semantictags aus DialogOS - e1,e2,e3
	static enum UserIntent{UserErklaerung, UserNenntTabuwort, Ja, Nein};
	UserIntent ourUserIntent;
	
	//Was System sagen kann
	Map <String, String> utterances;

	
	//baut systemaeu�erung zsm
	String buildString(String msg, String replacement1, String replacement2) {
		return msg.replace("{replacement}", replacement1).replace("{replacement2}", replacement2);
	}
	
	//lie�t am Anfang alle systemaeu�erungen aus datei ein
	Map<String, String> readSystemUtterances() {
		Map<String, String> utterances = new HashMap<String, String>();
		try {
			for(String line : IOUtils.readLines(this.getClass().getClassLoader().getResourceAsStream("utterances.txt"))){
				if (line.startsWith("#")) {
					continue;
				}
				String[] parts = line.split("=");
				String key = parts[0].trim();
				String utterance = parts[1].trim();
				utterances.put(key, utterance);
			}
			logger.info("Read " + utterances.keySet().size() + "utterances");
		} catch (IOException e) {
			logger.info("Could not read utterances: "+e.getMessage());
			System.err.println("Could not read utterances:"+e.getMessage());
		}
		return utterances;
	}

	//datenbank woraus tabuwort gezogen wird
	static String DBName = "Tabuspiel.db";
	private static Connection con = null;

	@Override
    public void onSessionStarted (SpeechletRequestEnvelope <SessionStartedRequest> requestEnvelope)
    {
		logger.info ("Alexa, ich m�chte Tabu spielen.");
		utterances = readSystemUtterances();
    }
	
    //Wir starten dialog
	//hole erstes Tabuwort aus der datenbank
	//Lie� die beg��ung vor und frage ob user regeln kennt oder nicht
	//wenn (if) user regeln kennt, starte spiel, (else) ansonten erkl�re regeln  
	// wir wollen dann antwort erkennen vom user
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope <LaunchRequest> requestEnvelope)
    {
	   logger.info("onLaunch");
	   recState = RecognitionState.Erklaerung;
	   selectTabuwort(); //daran soll tabuwort abgerufen werden
	   
       return askUserResponse (utterances.get("Hallo du spielst jetzt Tabu. Kennst du die Regeln oder soll sie dir erkl�ren?")+" "+tabuwort);
	   
    }
    
    //datenbankabfrage des tabuworts, funktioniert noch nicht wie es soll deshalb auskommentiert
    private void selectTabuwort() {
    	try {
    		con = DBConnect.getConnection();
    		//RANDOM ID WILL BE THE STORE THE RANDOMLY GENERATED NUMBER FOR WORDID IN DB
    		int randomId = (int) (Math.random() * 10);
    		Statement stmt = con.createStatement();
    		ResultSet rs = stmt
    					.executeQuery("SELECT * FROM Tabu_Woerter JOIN Tabu_Synonyme WHERE WortID=1");
    		// COULD BE
    		ResultSet rs1 = stmt.executeQuery("SELECT * FROM Tabu_Woerter JOIN Tabu_Synonyme WHERE WortID=" + randomId);
    		logger.info("Extractes .. from database");
    		//COULD BE
    		Logger.info("Extracts random word from database");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    } 
    
    
    //hier wird gespeichert was der User sagt.
    //String wird in Userrequest gespeichert
    //je nach cognition State reagiert das system unterschiedlich
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope)
	{
		IntentRequest request = requestEnvelope.getRequest();
		Intent intent = request.getIntent();
		userRequest = intent.getSlot("anything").getValue();
		logger.info("Received following text: [" + userRequest + "]");
		logger.info("recState is [" + recState + "]");
		SpeechletResponse resp = null;
		switch (recState) {
		case Erklaerung: resp = evaluateErklaerung(userRequest); break;
		case JaNein: resp = evaluateJaNein(userRequest); recState = RecognitionState.Erklaerung; break;
		default: resp = tellUserAndFinish("Erkannter Text: " + userRequest);
		}   
		return resp;
		
		 /* String line = "Erkl�re mir die Regeln"; //Muss in "onIntent" rein
		    String pattern = "Regeln";
		    Pattern p = Pattern.compile(pattern);
		    Matcher m = p.matcher(line); */ //hab versucht hier das mit Regel erkl�ren od. direkt starten einzubauen
	       /*
			if (m.matches()) {
				System.out.println("Undzwar bekommst du einen Tabubegriff und dazu noch weitere W�rter die du bei deinen Erkl�rungen nicht nennen darfst. Du musst mir das Wort erkl�ren und ich muss erraten k�nnen. Hast du die Regeln verstanden soweit?");
			} else {
				System.out.println("Okay, dein Tabuwort ist: ");
			} */ //zugeh�rige Ifschleife f�r Regel aber irgendwie hats nicht ganz geklappt

	}
    
    //if Alexa tabuwort err�t, dann fragen ob nochmal spielen
    //else if User nennt tabuwort, dann Stoppe und system.out.println(du hast tabuwort gesagt);
    // else 
    private SpeechletResponse evaluateErklaerung(String userRequest2) {
    	SpeechletResponse res = null;
    	recognizeUserIntent(userRequest2);
    	switch (ourUserIntent) {
    	case UserErklaerung:{
    		res = askUserResponse(utterances.get("erklaerung")); 
    		break;
    	}
    	case UserNenntTabuwort: {
    		break;
    	}
    	case Nein: {
    		break;
    	}
    	case Ja: {
    		switch(recState) {
    		
    		}
    		break;
    	}
    	default: {
    		res = askUserResponse(utterances.get(""));
    	}
    	}
    	
    	 String keyword = ".*Regeln.*";
    	 boolean match = Pattern.matches(keyword, userRequest2);
    	 if (match) {
    	 	
    	 }
    	 
    	return null;
		
	}

	private SpeechletResponse evaluateJaNein(String userRequest2) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case Ja: {
			selectTabuwort();
			res = askUserResponse(tabuwort); break;
		} case Nein: {
			res = tellUserAndFinish(utterances.get("goodbye")); break;
		} default: {
			res = askUserResponse(utterances.get(""));
		}
		}
		return res;
		
	}

	//Gucken ob User weitermachen will
    //Wenn ja, dann stelle Tabuwort
    //Wenn nein, dann beende das Spiel
    private SpeechletResponse UserErklaerung(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case UserErklaerung: {
			selectTabuwort();
			res = askUserResponse(tabuwort); break;
		} case UserNenntTabuwort: {
			res = tellUserAndFinish(utterances.get("goodbye")); break;
		} default: {
			res = askUserResponse(utterances.get(""));
		}
		}
		return res;
		
    }
    
    //THIS SHOULD JUST BE THE SAME AS GIVEN -- ALEXASKILLSPEECHLET.JAVA FROM WWM
    
    private void recognizeUserIntent(String userRequest2) {
		userRequest = userRequest.toLowerCase();
		String pattern1 = "(ich nehme )?(antwort )?(\\b[a-d]\\b)( bitte)?";
		String pattern2 = "(ich nehme )?(den )?publikumsjoker( bitte)?";
		String pattern3 = "(ich nehme )?(den )?(fiftyfifty|fünfzigfünfzig) joker( bitte)?";
		String pattern4 = "\\bnein\\b";
		String pattern5 = "\\bja\\b";
		Pattern p1 = Pattern.compile(pattern1);
		Matcher m1 = p1.matcher(userRequest);
		Pattern p2 = Pattern.compile(pattern2);
		Matcher m2 = p2.matcher(userRequest);
		Pattern p3 = Pattern.compile(pattern3);
		Matcher m3 = p3.matcher(userRequest);
		Pattern p4 = Pattern.compile(pattern4);
		Matcher m4 = p4.matcher(userRequest);
		Pattern p5 = Pattern.compile(pattern5);
		Matcher m5 = p5.matcher(userRequest);
		if (m1.find()) {
			String answer = m1.group(3);
			switch (answer) {
			case "a": ourUserIntent = UserIntent.Ja; break;
			case "b": ourUserIntent = UserIntent.Nein; break;
			case "c": ourUserIntent = UserIntent.UserErklaerung; break;
			case "d": ourUserIntent = UserIntent.UserNenntTabuwort; break;
			}
		} else if (m2.find()) {
			ourUserIntent = UserIntent.Publikum;
		} else if (m3.find()) {
			ourUserIntent = UserIntent.FiftyFifty;
		} else if (m4.find()) {
			ourUserIntent = UserIntent.No;
		} else if (m5.find()) {
			ourUserIntent = UserIntent.Yes;
		} else {
			ourUserIntent = UserIntent.Error;
		}
		logger.info("set ourUserIntent to " +ourUserIntent);
	}
 

	//tell the user smth or Alexa ends session after a 'tell'
    private SpeechletResponse tellUserAndFinish(String text)
	{
		// Create the plain text output.
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(text);

		return SpeechletResponse.newTellResponse(speech);
	}
	

	
	private SpeechletResponse responseWithFlavour(String text, int i) {

		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		switch(i){ 
		case 0: 
			speech.setSsml("<speak><amazon:effect name=\"whispered\">" + text + "</amazon:effect></speak>");
			break; 
		case 1: 
			speech.setSsml("<speak><emphasis level=\"strong\">" + text + "</emphasis></speak>");
			break; 
		case 2: 
			String half1=text.split(" ")[0];
			String[] rest = Arrays.copyOfRange(text.split(" "), 1, text.split(" ").length);
			speech.setSsml("<speak>"+half1+"<break time=\"3s\"/>"+ StringUtils.join(rest," ") + "</speak>");
			break; 
		case 3: 
			String firstNoun="erstes Wort buchstabiert";
			String firstN=text.split(" ")[3];
			speech.setSsml("<speak>"+firstNoun+ "<say-as interpret-as=\"spell-out\">"+firstN+"</say-as>"+"</speak>");
			break; 
		case 4: 
			speech.setSsml("<speak><audio src='soundbank://soundlibrary/transportation/amzn_sfx_airplane_takeoff_whoosh_01'/></speak>");
			break;
		default: 
			speech.setSsml("<speak><amazon:effect name=\"whispered\">" + text + "</amazon:effect></speak>");
		} 

		return SpeechletResponse.newTellResponse(speech);
	}


	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope)
	{
		logger.info("Das Tabuspiel ist zuende. Bis zum n�chsten Mal");
	}

	/**
	 * Tell the user something - the Alexa session ends after a 'tell'
	 */
	private SpeechletResponse response(String text)
	{
		// Create the plain text output.
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(text);

		return SpeechletResponse.newTellResponse(speech);
	}

	/**
	 * A response to the original input - the session stays alive after an ask request was send.
	 *  have a look on https://developer.amazon.com/de/docs/custom-skills/speech-synthesis-markup-language-ssml-reference.html
	 * @param text
	 * @return
	 */
	private SpeechletResponse askUserResponse(String text)
	{
		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		speech.setSsml("<speak>" + text + "</speak>");

		// reprompt after 8 seconds
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> Bist du noch da?</speak>");

		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);
	}


}