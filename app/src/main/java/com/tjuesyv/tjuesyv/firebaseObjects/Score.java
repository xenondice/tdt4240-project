package com.tjuesyv.tjuesyv.firebaseObjects;

/**
 * Created by Johannes on 4/15/2016.
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)

public class Score {

    String playerId;

    String gameId;
    int score;
    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    public Score(){}

    public Score (String gameId,String playerId){
        setGameId(gameId);
        setPlayerId(playerId);
        score=0;
    }

    public Score(String gameId,String playerId,int score){
        setGameId(gameId);
        setPlayerId(playerId);
        setScore(score);
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
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

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}
