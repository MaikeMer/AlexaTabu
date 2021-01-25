package com.amazon.customskill;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.customskill.AlexaSkillSpeechlet.RecognitionState;
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

import java.sql.*;public class AlexaServlet extends com.amazon.speech.speechlet.servlet.SpeechletServlet {
	
	  public static class AlexaSkillSpeechlet
	    implements SpeechletV2
	    {
	    	static Logger logger = LoggerFactory.getLogger(AlexaSkillSpeechlet.class);

			@Override
			public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
				// TODO Auto-generated method stub
				
			}

	    	
	    }
	
	/**
     * Diese Klasse NICHT Ã¤ndern!
     */
    private static final long serialVersionUID = 1L;

    public AlexaServlet() {
	    this.setSpeechlet(new AlexaSkillSpeechlet());
    }
	
    
	static Logger logger = LoggerFactory.getLogger(AlexaSkillSpeechlet.class);
	
	static int sum;
	static String answerOption1 = "";
	static String answerOption2 = "";
	static boolean publikumUsed;
	static boolean fiftyfiftyUsed;
	static String question = "";
	static String correctAnswer = "";
	
	public static String userRequest;
	
	//Datenbank einfügen
	
	

	
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope)
	{
		logger.info("Alexa ich möchte tabu spielen");
	}


	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope)
	{
		return askUserResponse("Hallo! Kennst du die Regeln?");
		
		if (return("Nein")); {
			
		    return askUserResponse("Ich werde dir ein Wort nennen, welches du erklären musst. Außerdem werde ich dir drei weitere Wörter nennen, die du bei deiner Erklärung nicht erwähnen darfst");
		    return newAskResponse("Ok, wir können anfangen");
		    else (return askUserResponse("Okay, dann lass uns anfangen");
		}
	}

	private boolean newAskResponse(String string) {
		// TODO Auto-generated method stub
		return false;
	}


	private SpeechletResponse askUserResponse(String string) {
		// TODO Auto-generated method stub
		return null;
	}


}