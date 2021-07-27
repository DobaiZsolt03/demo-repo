package client;

import javafx.beans.property.SimpleStringProperty;

public class QuestionsClass {
	private SimpleStringProperty question = new SimpleStringProperty("");
	private SimpleStringProperty subject = new SimpleStringProperty("");

	//Constructor
	public QuestionsClass() {
	}

	//GETTERS
	public String getQuestion() {
	    return question.get();
	}
	
	public String getSubject() {
		return subject.get();
	}

	//SETTERS
	public void setQuestion(String value) {
		question.set(value);
	}

	public void setSubject(String value) {
		subject.set(value);
	}
	
}
