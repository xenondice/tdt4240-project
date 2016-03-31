package com.tjuesyv.tjuesyv.firebaseObjects;

import com.firebase.client.ServerValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Game {
    private String gameCode;
    private Boolean active;
    private Boolean started;
    private Long createdAt;
    private Long round;
    private Long maxPlayers;
    private List<Player> players;

    public Game() {}

    public Game(String gameCode) {
        this.gameCode = gameCode;
        active = true;
        started = false;
        round = 1L;
        maxPlayers = 8L;
        players = new ArrayList<Player>();
    };

    public Map<String, String> getCreatedAt() {
        return ServerValue.TIMESTAMP;
    }

    public Long getCreatedAtLong() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getStarted() {
        return started;
    }

    public void setStarted(Boolean started) {
        this.started = started;
    }

    public Boolean getActive() {
        return active;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Long getRound() {
        return round;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void incrementRound() {
        round ++;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void setGameState(boolean state) {
        active = state;
    }

    public Long getMaxPlayers() {
        return maxPlayers;
    }
}
