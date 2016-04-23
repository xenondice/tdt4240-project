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
import com.tjuesyv.tjuesyv.firebaseObjects.Question;
import com.tjuesyv.tjuesyv.firebaseObjects.Score;
import com.tjuesyv.tjuesyv.gameModes.DefaultMode;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by RayTM on 08.04.2016.
 */
public class GameObserver {

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
    private String gameUid;

    private Game gameInfo;
    private Question activeQuestion;
    private Map<String, Player> activePlayers;
    private Map<String, Score> activeScores;
    private List<String> fullySyncedPlayers;
    private boolean startedListening;

    public GameObserver(GameActivity activityReference, GameMode gameMode) {

        // Assign variables
        startedListening = false;
        this.gameMode = gameMode;
        currentState = null;
        this.activityReference = activityReference;
        activeScores = new HashMap<>();
        activePlayers = new HashMap<>();
        fullySyncedPlayers = new ArrayList<>();
        activeQuestion = null;

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
        this.gameUid = gameUID;

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

        // Start listening for changes from the server
        ValueEventListener serverListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final Game oldGameInfo = gameInfo;
                    gameInfo = dataSnapshot.getValue(Game.class);

                    if (oldGameInfo == null) {
                        setListeners();
                        enterLobbyClient();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handleNewData(oldGameInfo);
                            }
                        }).start();
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
    synchronized private void handleNewData(Game oldGameInfo) {
        if (gameInfo.getQuestion() != oldGameInfo.getQuestion()) {
            final CountDownLatch waiter = new CountDownLatch(1);
            getFirebaseQuestionsReference().child("" + gameInfo.getQuestion()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    activeQuestion = dataSnapshot.getValue(Question.class);
                    waiter.countDown();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    activeQuestion = new Question("No question!", "No answer!");
                    waiter.countDown();
                }
            });

            try {
                waiter.await();
            } catch (InterruptedException e) {
                activeQuestion = new Question("No question!", "No answer!");
            }
        }

        if (gameInfo.getStateId() != oldGameInfo.getStateId()) {
            if (gameInfo.getStateId() == 0) enterLobbyClient();
            else if (gameInfo.getStateId() == 1) startNewRoundClient();
            else setActiveState(gameMode.getStates().get(gameInfo.getStateId()-1));
        }

        if (gameInfo.getGameModeId() != oldGameInfo.getGameModeId()) {
            changeGamemodeClient(gameInfo.getGameModeId());
        }

        if (gameInfo.getPlayers().size() > oldGameInfo.getPlayers().size()) {
            notifyPlayer(gameInfo.getPlayers().get(gameInfo.getPlayers().size()-1));
        }
    }

    /**
     * Setup listeners
     * Start listeners for necessary values
     * After one is added here, make a function in GameState, which you can then override
     * Also check if the value was actually changed to a new value, or if it the same
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
                        if (activePlayers.put(playerId, player) == null) {
                            notifyPlayer(playerId);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {}
                });

                getFirebaseScoresReference().child(gameUid).child(playerId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot scoreSnapshot) {
                        // Get the player object
                        Score score = scoreSnapshot.getValue(Score.class);
                        // Store info for future reference
                        if (activeScores.put(playerId, score) == null) {
                            notifyPlayer(playerId);
                        }
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

    public Question getQuestion() {
        return activeQuestion;
    }

    /**
     * Create a new instance of the class and set the correct view for the local player
     */
    private void setActiveState(final Class<? extends GameState> state) {
        final GameObserver observer = this;
        getActivityReference().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (currentState != null) currentState.onExit();
                    currentState = state.getConstructor(observer.getClass()).newInstance(observer);
                    rootFlipper.setDisplayedChild(currentState.getViewId());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.getCause().printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        });
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

    private void startNewRoundClient() {
        setActiveState(gameMode.getStates().get(0));
    }

    private void notifyPlayer(final String playerId) {
        if (gameInfo.getPlayers().contains(playerId) &&
                activePlayers.get(playerId) != null &&
                activeScores.get(playerId) != null) {
            fullySyncedPlayers.add(playerId);
            getActivityReference().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentState.newPlayerJoined(playerId);
                }
            });
        }
    }

    /**
     * Move server to next state
     */
    private void nextStateServer() {
        getFirebaseGameReference().child("stateId").setValue(gameInfo.getStateId() + 1);
    }

    public List<String> getActivePlayers() {
        return fullySyncedPlayers;
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
        // Choose a question
        getFirebaseQuestionsReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int randomQuestion = (int) Math.floor(Math.random() * dataSnapshot.getChildrenCount());

                String tempGameMaster = null;

                // Choose game master
                if (!gameInfo.getGameMaster().isEmpty())
                    for (int i = 0; i < gameInfo.getPlayers().size(); i++) {
                        String player = gameInfo.getPlayers().get(i);
                        if (gameInfo.getGameMaster().equals(player)) {
                            int newGmId = (i + 1) % gameInfo.getPlayers().size();
                            tempGameMaster = gameInfo.getPlayers().get(newGmId);
                            break;
                        }
                    }

                if (tempGameMaster == null) {
                    tempGameMaster = gameInfo.getGameHost();
                }

                // Set variables
                Map<String, Object> data = new HashMap<>();
                data.put("question", randomQuestion);
                data.put("gameMaster", tempGameMaster);
                data.put("started", true);
                data.put("round", gameInfo.getRound() + 1);
                data.put("stateId", 1);
                getFirebaseGameReference().updateChildren(data);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
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
}
