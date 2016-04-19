package com.tjuesyv.tjuesyv.gameHandlers;

import com.tjuesyv.tjuesyv.firebaseObjects.Game;
import com.tjuesyv.tjuesyv.gameModes.DefaultMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by RayTM on 08.04.2016.
 */
public abstract class GameMode {

    public static final int DEFAULT_MODE_ID = 0;

    /**
     * Get a instance of the correct gamemode given a id from the firebase server
     * IMPORTANT: add new gamemodes in this list, otherwise the other clients won't know what
     * gamemode you are in
     */
    public static GameMode getGameModeFromId(int firebaseId) {
        switch (firebaseId) {
            case DEFAULT_MODE_ID:
                return new DefaultMode();
            default:
                return null;
        }
    }

    /**
     * Return true if the game is over given the round info, called once on every round end
     * This can be when a player has a certain score, when a number of rounds is reached,
     * When a certain time limit is reached and so on..
     */
    public abstract boolean isGameOver(Game game);

    /**
     * Return the different states of the game mode,
     * not included the lobby
     * IMPORTANT: the firebase state value equals the index of this list, so order correctly
     */
    public abstract List<Class<? extends GameState>> getStates();

    /**
     * Return the lobby state of the game mode
     */
    public abstract Class<? extends GameState> getLobby();
}
