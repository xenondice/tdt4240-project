package com.tjuesyv.tjuesyv.gameHandlers;

import android.app.usage.NetworkStats;
import android.content.Context;
import android.content.Intent;
import android.widget.ViewFlipper;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.GameActivity;
import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.firebaseObjects.Game;
import com.tjuesyv.tjuesyv.states.LobbyState;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by RayTM on 08.04.2016.
 */
public class GameHandler {

    @Bind(R.id.rootFlipper) ViewFlipper rootFlipper;

    private GameMode gameMode;
    private GameActivity activityReference;
    private int currentState;
    private int currentRound;

    private String gameUID;
    private Firebase rootRef;
    private Firebase gamesRef;
    private Firebase usersRef;
    private Firebase currentGameRef;
    private Firebase currentUserRef;
    private AuthData authData;
    private boolean isHost;

    public GameHandler(GameActivity activityReference, GameMode gameMode) {

        // Assign variables
        isHost = false;
        this.gameMode = gameMode;
        currentState = 0;
        currentRound = 0;
        this.activityReference = activityReference;

        // Bind handler
        gameMode.bindHandler(this);

        // Setup ButterKnife
        ButterKnife.bind(this, activityReference);

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

        // Check if host
        setGameHost();
    }

    /**
     * Check whether the player is the host
     */
    private void setGameHost() {
        getFirebaseGameReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Game game = dataSnapshot.getValue(Game.class);
                    isHost = game.getGameHost().equals(authData.getUid());
                    //TODO: Prettify (Make resource loader function)
                    enterLobby();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    /**
     * Get the calling activity
     * @return
     */
    public GameActivity getActivityReference() {
        return activityReference;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    /**
     * Progress in the game
     */
    public void nextState() {
        //TODO: Prettify (enums?)
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
            rootFlipper.setDisplayedChild(gameMode.getStates()[currentState-1].getViewId());
            gameMode.getStates()[currentState-1].onEnter();
        }
    }

    /**
     * Enter lobby view
     */
    public void enterLobby() {
        gameMode.getLobby().onEnter();
    }

    /**
     * Get if current player is the host
     * @return
     */
    public boolean isHost() {
        return isHost;
    }

    /**
     * Get the firebase reference to the current game
     * @return
     */
    public Firebase getFirebaseGameReference() {
        return currentGameRef;
    }

    /**
     * Get the firebase reference to the active users
     * @return
     */
    public Firebase getFirebaseUsersReference() {
        return usersRef;
    }

    public AuthData getFirebaseAuthenticationData() {
        return authData;
    }

    public int getNumberOfRounds() {
        return gameMode.getNumberOfRounds();
    }
}
