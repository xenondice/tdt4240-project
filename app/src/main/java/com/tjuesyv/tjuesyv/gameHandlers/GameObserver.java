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
import com.tjuesyv.tjuesyv.gameModes.DefaultMode;
import com.tjuesyv.tjuesyv.states.LobbyState;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by RayTM on 08.04.2016.
 */
public class GameObserver implements Closeable {

    @Bind(R.id.rootFlipper) ViewFlipper rootFlipper;

    private GameMode gameMode;
    private GameActivity activityReference;
    private GameState currentState;

    private Firebase rootRef;
    private Firebase usersRef;
    private Firebase scoresRef;
    private Firebase questionsRef;
    private Firebase currentGameRef;
    private Firebase currentUserRef;
    private Firebase roundAnswersRef;
    private AuthData authData;

    private ValueEventListener serverListener;
    private Game gameInfo;
    private boolean startedListening;

    public GameObserver(GameActivity activityReference, GameMode gameMode) {

        // Assign variables
        startedListening = false;
        this.gameMode = gameMode;
        currentState = null;
        this.activityReference = activityReference;

        // Setup ButterKnife
        ButterKnife.bind(this, activityReference);
    }

    /**
     * Make new observer with the default gamemode
     */
    public GameObserver(GameActivity activityReference) {
        this(activityReference, new DefaultMode());
    }

    /**
     * Start observing the game
     */
    public void activate() {

        // Don't activate more than once
        if (startedListening) return;
        startedListening = true;

        // Get ID
        Intent activityIntent = activityReference.getIntent();
        String gameUID = activityIntent.getStringExtra("GAME_UID");

        // Create main Firebase ref
        rootRef = new Firebase(activityReference.getResources().getString(R.string.firebase_url));

        // Get Firebase authentication
        authData = rootRef.getAuth();

        // Setup other Firebase references
        Firebase gamesRef = rootRef.child("games");
        usersRef = rootRef.child("users");
        scoresRef = rootRef.child("scores");
        questionsRef = rootRef.child("questions");
        currentGameRef = gamesRef.child(gameUID);
        currentUserRef = usersRef.child(authData.getUid());
        roundAnswersRef = currentGameRef.child("answers");

        // Set random initial question
        setRandomQuestion();

        // Start listening for changes from the server
        serverListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Game oldGameInfo = gameInfo;
                    gameInfo = dataSnapshot.getValue(Game.class);

                    if (oldGameInfo == null) {
                        //startListeners();
                        enterLobbyClient();
                    } else {
                        handleNewData(oldGameInfo);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                throw new IllegalStateException("Couldn't get data!");
            }
        };
        getFirebaseGameReference().addValueEventListener(serverListener);
    }

    /**
     * TODO: Update description
     * See what is new and do something
     */
    private void handleNewData(Game oldGameInfo) {
        if (gameInfo.getStateId() != oldGameInfo.getStateId()) {
            if (gameInfo.getStateId() == 0) enterLobbyClient();
            else setActiveState(gameMode.getStates().get(gameInfo.getStateId()-1));
        }

        if (gameInfo.getGameModeId() != oldGameInfo.getGameModeId()) {
            changeGamemodeClient(gameInfo.getGameModeId());
        }
    }

    /**
     * TODO: Is this necessary?
     * Start listeners for necessary values
     * After one is added here, make a function in GameState, which you can then overrride
     * Also check if the value was actually changed to a new value, or if it the same
     */
    private void startListeners() {
        getFirebaseGameReference().child("stateId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    /**
     * Create a new instance of the class and set the correct view for the local player
     */
    private void setActiveState(Class<? extends GameState> state) {
        try {
            currentState = state.getConstructor(this.getClass()).newInstance(this);
            rootFlipper.setDisplayedChild(currentState.getViewId());
        } catch (InstantiationException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Change server to a new gamemode if in lobby
     * Only the host can call this function since it changes for everyone
     */
    public void changeGamemodeServer(int firebaseGameModeId) {
        if (!isInLobby() || !isHost()) return;
        getFirebaseGameReference().child("gameModeId").setValue(firebaseGameModeId);
    }

    private void changeGamemodeClient(int firebaseGameModeId) {
        gameMode = GameMode.getGameModeFromId(firebaseGameModeId);
        if (gameMode == null) throw new IllegalStateException("Server switched to an invalid gamemode!");
        enterLobbyClient();
    }

    private void enterLobbyClient() {
        setActiveState(gameMode.getLobby());
    }

    /**
     * Get the calling activity
     */
    public GameActivity getActivityReference() {
        return activityReference;
    }

    public int getCurrentRound() {
        return gameInfo.getRound();
    }

    /**
     * Progress server in the game
     * Only one player should be able to call this function since it changes for everyone
     */
    public void progressServer() {
        //TODO: Make observer, have functions called in gamemode instead of having access to observer
        if (isInLobby()) {
            startNewRoundServer();
        } else {
            if (onLastState()) {
                if (gameMode.isGameOver(gameInfo)) enterLobbyServer();
                else startNewRoundServer();
            } else {
                nextStateServer();
            }
        }
    }

    /**
     * Move server to next state
     */
    private void nextStateServer() {
        getFirebaseGameReference().child("stateId").setValue(gameInfo.getStateId() + 1);
    }

    /**
     * Get whether server is on the gamemode's last state
     */
    private boolean onLastState() {
        return gameInfo.getStateId() >= gameMode.getStates().size();
    }

    /**
     * Make server start new round
     */
    private void startNewRoundServer() {
        String tempGameMaster = null;

        // Choose game master
        for (int i = 0; i < gameInfo.getPlayers().size(); i++) {
            String player = gameInfo.getPlayers().get(i);
            if (gameInfo.getGameMaster().equals(player)) {
                int newGmId = (i + 1) % gameInfo.getPlayers().size();
                tempGameMaster = gameInfo.getPlayers().get(newGmId);
                break;
            }
        }
        if (tempGameMaster == null) {
            System.out.println("No existing game master found, setting as host");
            tempGameMaster = gameInfo.getGameHost();
        }

        // Set variables
        Map<String, Object> data = new HashMap<>();
        data.put("gameMaster", tempGameMaster);
        data.put("started", true);
        data.put("round", gameInfo.getRound() + 1);
        data.put("stateId", 1);
        getFirebaseGameReference().updateChildren(data);
    }

    /**
     * See if the server is in the lobby state
     */
    private boolean isInLobby() {
        return gameInfo.getStateId() == 0;
    }

    /**
     * Make server enter lobby view
     */
    private void enterLobbyServer() {
        Map<String, Object> data = new HashMap<>();
        data.put("started", false);
        data.put("round", 0);
        data.put("stateId", 0);
        getFirebaseGameReference().updateChildren(data);
    }

    /**
     * Get if current player is the host
     */
    public boolean isHost() {
        return gameInfo.getGameHost().equals(authData.getUid());
    }

    /**
     * Get if current player is the game master
     */
    public boolean isGameMaster() {
        return gameInfo.getGameMaster().equals(authData.getUid());
    }

    /**
     * Helper method to generate a random question ID
     * @return
     */
    public void setRandomQuestion() {
        getFirebaseQuestionsReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int random = (int) Math.floor(Math.random() * dataSnapshot.getChildrenCount());
                getFirebaseGameReference().child("question").setValue(random);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /**
     * Get the firebase reference to the current game
     */
    public Firebase getFirebaseGameReference() {
        return currentGameRef;
    }

    /**
     * Get the firebase reference to the active users
     */
    public Firebase getFirebaseUsersReference() {
        return usersRef;
    }

    /**
     * Get the firebase reference to the scores
     * @return
     */
    public Firebase getFirebaseScoresReference() {
        return scoresRef;
    }

    /**
     * Get the firebase reference to the answers in the current round
     * @return
     */
    public Firebase getFirebaseAnswersReference() {
        return roundAnswersRef;
    }

    /**
     * Get the firebase reference to the questions
     * @return
     */
    public Firebase getFirebaseQuestionsReference() {
        return questionsRef;
    }

    /**
     * Get the root firebase object
     */
    public Firebase getFirebaseRootReference(){return rootRef;};


    /**
     * Get the firebase authentication object
     */

    public AuthData getFirebaseAuthenticationData() {
        return authData;
    }

    /**
     * Get the whole Game object
     */
    public Game getGameInfo(){
        return gameInfo;
    }
    public void close() {
        getFirebaseGameReference().removeEventListener(serverListener);
    }
}
