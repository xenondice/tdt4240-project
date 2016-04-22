package com.tjuesyv.tjuesyv.gameHandlers;

import com.tjuesyv.tjuesyv.firebaseObjects.Game;
import com.tjuesyv.tjuesyv.firebaseObjects.Player;

public abstract class GameState {

    protected GameObserver observer;

    /**
     * Called once the state is entered
     * @param observer
     */
    public GameState(GameObserver observer) {
        this.observer = observer;
    }

    /**
     * Return the layout id from the activity game xml file,
     * that is, what number the view has where 0 is top, and counting upwards
     * the further down in views you go
     * Rememer to differentiate the views for the game master and player
     * @return
     */
    public abstract int getViewId();

    /**
     * Continue the game to the nest state
     */
    public void nextState() {
        observer.progressServer();
    }

    /**
     * Get the most current game information
     */
    public Game getGameInfo() {
        return observer.getGameInfo();
    }

    /**
     * Check if user is game master
     */
    public boolean isGameMaster() {
        return observer.isGameMaster();
    }

    /**
     * Check if user is host of the game
     */
    public boolean isGameHost() {
        return observer.isHost();
    }

    /**
     * Fired once a player's score is changed
     */
    public void playerScoreChanged(String playerId) {}

    /**
     * Fired once the state is changing
     */
    public void onExit() {}

    /**
     * Fired once a new player joined the game
     */
    public void newPlayerJoined(String playerId) {}


    /**
     * Fired once a player leaves the game
     */
    public void playerLeft(String playerId) {}
}
