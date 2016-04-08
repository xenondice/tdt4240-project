package com.tjuesyv.tjuesyv.states;

import com.tjuesyv.tjuesyv.gameHandlers.GameState;

/**
 * Created by RayTM on 08.04.2016.
 * Here all players are faced with a fancy and exciting representation of the score so far into the game
 * Should also show a slightly different view if it's the final game
 */
public class ScoreState extends GameState {
    private static final int MAIN_VIEW = 5;

    @Override
    public int getViewId() {
        return 3;
    }

    @Override
    public void onEnter() {

    }
}
