package com.tjuesyv.tjuesyv.firebaseObjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class Game {
    private String gameCode;
    private String gameHost;
    private Boolean active;
    private Boolean started;
    private int round;
    private int maxPlayers;
    private Map<String, String> createdAt;
    private Map<String, Object> players = new HashMap<String, Object>();

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    public Game() {}

    public Game(String gameCode, String gameHost) {
        this.gameCode = gameCode;
        this.gameHost = gameHost;
        active = true;
        started = false;
        round = 1;
        maxPlayers = 8;
        createdAt = ServerValue.TIMESTAMP;
    };

    @JsonIgnore
    public Map<String, String> getCreatedAt() {
        return createdAt;
    }

    public String getGameHost() { return gameHost; }

    public Boolean getStarted() {
        return started;
    }

    public void setStarted(Boolean started) {
        this.started = started;
    }

    public Boolean getActive() {
        return active;
    }

    public Map<String, Object> getPlayers() {
        return players;
    }

    public int getRound() {
        return round;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void incrementRound() {
        round ++;
    }

    public void addPlayer(String playerId) {
        players.put(playerId, true);
    }

    public void setGameState(boolean state) {
        active = state;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
