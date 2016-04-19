package com.tjuesyv.tjuesyv.firebaseObjects;

public class Score {
    int score;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    public Score(){}

    public Score(int score){
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public  void incrementScore(){
        score++;
    }
}
