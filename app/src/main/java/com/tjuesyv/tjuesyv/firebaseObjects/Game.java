package com.tjuesyv.tjuesyv.firebaseObjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.ServerValue;
import com.tjuesyv.tjuesyv.gameHandlers.GameMode;

import java.util.HashMap;
import java.util.Map;

public class Game {
    private String gameCode;
    private String gameHost;
    private String gameMaster;
    private Boolean active;
    private Boolean started;
    private int round;
    private int stateId;
    private int gameModeId;
    private int maxPlayers;
    private Map<String, String> createdAt;
    private Map<String, Object> players = new HashMap<String, Object>();

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    public Game() {}

    public Game(String gameCode, String gameHost) {
        this.gameCode = gameCode;
        this.gameHost = gameHost;
        gameMaster = gameHost;
        active = true;
        started = false;
        round = 0;
        maxPlayers = 8;
        createdAt = ServerValue.TIMESTAMP;
        stateId = 0;
        gameModeId = GameMode.DEFAULT_MODE_ID;
    }

    @JsonIgnore
    public Map<String, String> getCreatedAt() {
        return createdAt;
    }

    public String getGameHost() { return gameHost; }

    public String getGameMaster() { return gameMaster; }

    public int getGameModeId() { return gameModeId; }

    public int getStateId() { return stateId; }

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
