package com.tjuesyv.tjuesyv.states;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.firebaseObjects.Player;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;
import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.firebaseObjects.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LobbyState extends GameState {

    @Bind(R.id.gameCodeTextView) TextView gameCodeTextView;
    @Bind(R.id.startedTextView) TextView startedTextView;
    @Bind(R.id.activeTextView) TextView activeTextView;
    @Bind(R.id.startGameButton) Button startGameButton;
    @Bind(R.id.playerListView) ListView playersListView;

    private static final int MAIN_VIEW = 0;

    @Override
    public int getViewId() {
        return MAIN_VIEW;
    }

    @Override
    public void onEnter() {

        // Setup ButterKnife
        ButterKnife.bind(this, handler.getActivityReference());

        // Displays game info
        setGameInfo();

        // Displays players in a listView
        setPlayerList();
    }

    @OnClick(R.id.startGameButton)
    protected void startGameButton() {
        nextState();
    }

    /**
     * Populates game info textViews.
     */
    private void setGameInfo() {
        handler.getFirebaseGameReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Game game = dataSnapshot.getValue(Game.class);
                gameCodeTextView.setText("Game code: " + game.getGameCode());
                startedTextView.setText("Started: " + game.getStarted());
                activeTextView.setText("Active: " + game.getActive());

                // If we are not the game host, disable start button and change text
                if (!game.getGameHost().equals(handler.getFirebaseAuthenticationData().getUid())) {
                    startGameButton.setText("Waiting on host to start game...");
                    startGameButton.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /**
     * Updates player list when players are added or removed.
     */
    private void setPlayerList() {
        // Initialize list of players
        final List<Map<String, String>> players = new ArrayList<Map<String, String>>();

        // Create a new simple adapter to represent the players in the list view
        final SimpleAdapter simpleAdapter = new SimpleAdapter(handler.getActivityReference(),
                players,
                android.R.layout.simple_list_item_2,
                new String[] {"nickname", "role"},
                new int[] {android.R.id.text1, android.R.id.text2});
        // Assign adapter to ListView
        playersListView.setAdapter(simpleAdapter);

        // Get the players of the current game
        handler.getFirebaseGameReference().child("players").addChildEventListener(new ChildEventListener() {
            // If player has joined
            @Override
            public void onChildAdded(final DataSnapshot playerInGameSnapshot, String s) {
                // Look up player in users reference
                handler.getFirebaseUsersReference().child(playerInGameSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot playerSnapshot) {
                        Player player = playerSnapshot.getValue(Player.class);

                        // Create Map to represent player in the list
                        Map<String, String> playerItem = new HashMap<String, String>(2);
                        playerItem.put("nickname", player.getNickname());
                        // Update role
                        if (playerSnapshot.getKey().equals(handler.getFirebaseAuthenticationData().getUid()) && player.isGameHostInGame(handler.getFirebaseGameReference().getKey())){
                            // Player is us and we are the game host
                            playerItem.put("role", "You are the game host");
                        } else if (playerSnapshot.getKey().equals(handler.getFirebaseAuthenticationData().getUid())) {
                            // Player is us
                            playerItem.put("role", "You");
                        } else if (player.isGameHostInGame(handler.getFirebaseGameReference().getKey())) {
                            // Other player is game host
                            playerItem.put("role", "Game host");
                        }

                        // Add player to players list and notify
                        players.add(playerItem);
                        simpleAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }

            // If player is removed
            @Override
            public void onChildRemoved(DataSnapshot playerInGameSnapshot) {
                handler.getFirebaseUsersReference().child(playerInGameSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot playerSnapshot) {
                        Player player = playerSnapshot.getValue(Player.class);
                        // Create a KV to lookup in the players list
                        Map<String, String> playerItem = new HashMap<String, String>(2);
                        playerItem.put("nickname", player.getNickname());
                        // Remove the player from players list and notify
                        players.remove(players.indexOf(playerItem));
                        simpleAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }
}
