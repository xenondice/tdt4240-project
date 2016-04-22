package com.tjuesyv.tjuesyv.firebaseObjects;

public class Question {
    String question;
    String answer;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    public Question() {}

    public String getQuestion() { return question; }

    public String getAnswer() { return answer; }

}
