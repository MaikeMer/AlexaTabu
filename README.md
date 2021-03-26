# AlexaTabu

Damit der Skill läuft: 
Invocationname = tabutabutabu tabu -> Dadurch wird im AlexaDeveloper der Skill aktiviert.
Ngrok und Tomcat sollten akitiviert sein. 

Ablauf im Alexa Developer:
1. Begrüßung beginnt. 
2. Als richtige Erklärung des Tabuworts Ampel funktioniert zunächst nur die Erklärung "Es befindet sich im Verkehr".
Andere Erklärungen werden zunächst als Tabuwort gewertet, dies haben wir so belassen aufgrund der Evaluation.
Zwischenzeitlich haben wir an dem Filter gearbeitet, damit dieser funktioniert.
3. Es wird gefragt ob man weiterspielen will, man kann nur Ja oder Nein antworten.
4. Bei Ja wird ein neues Tabuwort ausgegeben
5. Bei Nein wird das Spiel beendet
6. Wenn man von vorne beginnen will, muss man den Alexa Developer einmal aktualisieren


Damit der Skill weiter fortgeführt werden kann:
- Die ArrayList sollte angepasst werden, sprich die Tabuwörter aus der Datenbank in der Tabulist.java speichern 
und auf diese dann im AlexaSkillSpeechlet zugreifen. Dadurch würde der Filter der Tabuwörter besser funktionieren.
Der Filter sollte erkennen, welches Tabuwort zum jetzigen Zeitpunkt der User hat. (Ist im Javacode enthalten)
- Regelabfrage: Möchtest du die Regeln wissen, oder soll direkt das Spiel starten? 
- evaluateErklaerung besser optimieren
- evaluateJaNein, bei case Ja: Wenn das Tabuwort ausgegeben wird, dass man normal weiterspielen kann
- Erweiterung der Datenbank, diese mit mehr Tabuwörtern einspeisen
- Randomisierte Auswahl der Tabuwörter (ist im Javacode auskommentiert)
- Das Alexa Gegenfragen und Gegenantworten stellt
- Der Stimmwechsel von Alexa vom "Erklärmodus" zum "Spielmodus", dass sich dieser wechselt
