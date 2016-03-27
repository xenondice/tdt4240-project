package com.tjuesyv.tjuesyv;

import com.firebase.client.ServerValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Game {
    private String gameID;
    private Boolean active;
    private Boolean started;
    private Long createdAt;
    private Long round;
    private List<Player> players;

    public Game(String gameID) {
        this.gameID = gameID;
        active = true;
        started = false;
        round = 1L;
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

    public String getGameID() {
        return gameID;
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
}
