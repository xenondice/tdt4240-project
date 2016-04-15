package com.tjuesyv.tjuesyv.gameHandlers;

/**
 * Created by RayTM on 08.04.2016.
 */
public abstract class GameMode {

    /**
     * Return the total number of times the game should go through
     * the given states before returning to the lobby
     * @return
     */
    public abstract int getNumberOfRounds();

    /**
     * Return the different states of the game mode,
     * not included the lobby
     * IMPORTANT: the states should be stored as they need a GameObserver
     * which is given to them once a GameObserver is created
     * @return
     */
    public abstract GameState[] getStates();

    /**
     * Return the lobby state of the game mode
     * IMPORTANT: the state should be stored as it needs a GameObserver
     * which is given to it once a GameObserver is created
     * @return
     */
    public abstract GameState getLobby();

    /**
     * Called internally by GameObserver before starting the game
     * @param handler
     */
    void bindHandler(GameObserver handler) {
        for (GameState state : getStates()) {
            state.bindHandler(handler);
        }
        getLobby().bindHandler(handler);
    }
}
