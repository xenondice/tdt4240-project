package com.tjuesyv.tjuesyv.gameHandlers;

import android.content.Context;
import android.content.Intent;
import android.widget.ViewFlipper;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.tjuesyv.tjuesyv.GameActivity;
import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.states.LobbyState;

import butterknife.Bind;

/**
 * Created by RayTM on 08.04.2016.
 */
public class GameHandler {

    @Bind(R.id.rootFlipper) ViewFlipper rootFlipper;

    private GameMode gameMode;
    private GameActivity activityReference;
    private int currentState;
    private int currentRound;

    public String gameUID;
    public Firebase rootRef;
    public Firebase gamesRef;
    public Firebase usersRef;
    public Firebase currentGameRef;
    public Firebase currentUserRef;
    public AuthData authData;

    public GameHandler(GameActivity activityReference, GameMode gameMode) {
        this.gameMode = gameMode;
        gameMode.bindHandler(this);
        currentState = 0;
        currentRound = 0;
        this.activityReference = activityReference;

        // Get ID
        Intent activityIntent = activityReference.getIntent();
        gameUID = activityIntent.getStringExtra("GAME_UID");

        // Create main Firebase ref
        rootRef = new Firebase(activityReference.getResources().getString(R.string.firebase_url));

        // Get Firebase authentication
        authData = rootRef.getAuth();

        // Setup other Firebase references
        gamesRef = rootRef.child("games");
        usersRef = rootRef.child("users");
        currentGameRef = gamesRef.child(gameUID);
        currentUserRef = usersRef.child(authData.getUid());
    }

    /**
     * Get the calling activity
     * @return
     */
    public GameActivity getActivityReference() {
        return activityReference;
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
            rootFlipper.setDisplayedChild(gameMode.getLobby().getViewId());
            gameMode.getLobby().onEnter();
        } else {
            rootFlipper.setDisplayedChild(gameMode.getStates()[currentState].getViewId());
            gameMode.getStates()[currentState].onEnter();
        }
    }

    /**
     * Start the game
     */
    public void startGame() {
        gameMode.getLobby().onEnter();
    }
}
