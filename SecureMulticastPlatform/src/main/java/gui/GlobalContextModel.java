package gui;

import client.QuestionsClass;

public class GlobalContextModel {
	private final static GlobalContextModel instance = new GlobalContextModel();

    public static GlobalContextModel getInstance() {
        return instance;
    }

    private QuestionsClass modelClass = new QuestionsClass();

    public QuestionsClass currentQuestion() {
        return modelClass;
    }
    
    public QuestionsClass currentSubject() {
    	return modelClass;
    }
}
