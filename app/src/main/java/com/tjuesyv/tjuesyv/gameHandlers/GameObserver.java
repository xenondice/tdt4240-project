package com.tjuesyv.tjuesyv.gameHandlers;

import android.content.Intent;
import android.widget.ViewFlipper;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.GameActivity;
import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.firebaseObjects.Game;
import com.tjuesyv.tjuesyv.firebaseObjects.Player;
import com.tjuesyv.tjuesyv.firebaseObjects.Score;
import com.tjuesyv.tjuesyv.gameModes.DefaultMode;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Firebase currentGameRef;
    private Firebase currentUserRef;
    private AuthData authData;

    private Game gameInfo;
    private Map<String, Player> activePlayers;
    private Map<String, Score> activeScores;
    private List<ValueEventListener> listeners;
    private boolean startedListening;

    public GameObserver(GameActivity activityReference, GameMode gameMode) {

        // Assign variables
        startedListening = false;
        this.gameMode = gameMode;
        currentState = null;
        this.activityReference = activityReference;
        activeScores = new HashMap<>();
        activePlayers = new HashMap<>();
        listeners = new ArrayList<>();

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
        currentGameRef = gamesRef.child(gameUID);
        currentUserRef = usersRef.child(authData.getUid());

        // Start listening for changes from the server
        ValueEventListener serverListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Game oldGameInfo = gameInfo;
                    gameInfo = dataSnapshot.getValue(Game.class);

                    if (oldGameInfo == null) {
                        setListeners();
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
        listeners.add(serverListener);
    }

    /**
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
     * Setup listeners
     */
    public void setListeners() {
        getFirebaseGameReference().child("players").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Look up player in users reference
                final String playerId = (String) dataSnapshot.getValue();

                getFirebaseUsersReference().child(playerId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot playerSnapshot) {
                        // Get the player object
                        Player player = playerSnapshot.getValue(Player.class);
                        activePlayers.put(playerId, player);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {}
                });

                getFirebaseScoresReference().child(playerId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot scoreSnapshot) {
                        // Get the player object
                        Score score = scoreSnapshot.getValue(Score.class);
                        // Store info for future reference (updated last so notify here)
                        if (activeScores.put(playerId, score) == null)
                            currentState.newPlayerJoined(playerId);
                        else currentState.playerScoreChanged(playerId);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {}
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String playerId = (String) dataSnapshot.getValue();
                activePlayers.remove(playerId);
                activeScores.remove(playerId);
                currentState.playerLeft(playerId);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    public Player getPlayerFromId(String playerId) {
        return activePlayers.get(playerId);
    }

    public Score getScoreForPlayer(String playerId) {
        return activeScores.get(playerId);
    }

    /**
     * Create a new instance of the class and set the correct view for the local player
     */
    private void setActiveState(Class<? extends GameState> state) {
        try {
            if (currentState != null) currentState.onExit();
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
        //TODO: Make observer, have functions called in game state instead of having access to observer
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
     * Get if current player is the host
     */
    public boolean isGameMaster() {
        return gameInfo.getGameMaster().equals(authData.getUid());
    }

    /**
     * Get current game info
     */
    public Game getGameInfo() {
        return gameInfo;
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
     * Get the root firebase object
     */
    public Firebase getFirebaseRootReference(){return rootRef;};

    /**
     * Get the firebase authentication object
     */
    public AuthData getFirebaseAuthenticationData() {
        return authData;
    }

    public void close() {
        for (ValueEventListener listener : listeners) {
            getFirebaseRootReference().removeEventListener(listener);
        }
    }
}
