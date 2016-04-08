package com.tjuesyv.tjuesyv.gameHandlers;

import android.content.Context;
import android.widget.ViewFlipper;

import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.states.LobbyState;

import butterknife.Bind;

/**
 * Created by RayTM on 08.04.2016.
 */
public class GameHandler {

    @Bind(R.id.rootFlipper) ViewFlipper rootFlipper;

    private GameMode gameMode;
    private GameState lobby;
    private Context context;
    private int currentState;
    private int currentRound;

    public GameHandler(GameMode gameMode, Context context) {
        this.gameMode = gameMode;
        gameMode.bindHandler(this);
        lobby = new LobbyState();
        currentState = 0;
        currentRound = 0;
        this.context = context;
    }

    /**
     * Progress in the game
     */
    public void nextState() {

        // State 0 is lobby, rest is the states in gamemode
        currentState++;
        if (currentState - 1 >= gameMode.getStates().length) {
            currentRound++;
            if (currentRound >= gameMode.getNumberOfRounds()) {
                // Game finished
                currentState = 0;
                currentRound = 0;
            } else {
                currentState = 1;
            }
        }

        if (currentState == 0) {
            rootFlipper.setDisplayedChild(lobby.getViewId());
            lobby.onEnter();
        } else {
            rootFlipper.setDisplayedChild(gameMode.getStates()[currentState-1].getViewId());
            gameMode.getStates()[currentState-1].onEnter();
        }
    }

    /**
     * Start the game
     */
    public void startGame() {
        lobby.onEnter();
    }
}
