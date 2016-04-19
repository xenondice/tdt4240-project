package com.tjuesyv.tjuesyv.states;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
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
import butterknife.OnItemLongClick;

public class LobbyState extends GameState {

    @Bind(R.id.gameCodeTextView) TextView gameCodeTextView;
    @Bind(R.id.startedTextView) TextView startedTextView;
    @Bind(R.id.startGameButton) Button startGameButton;
    @Bind(R.id.playerListView) ListView playersListView;

    private static final int MAIN_VIEW = 0;
    private final List<Map<String, String>> playersList = new ArrayList<Map<String, String>>();

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

        // Displays playersList in a listView
        setPlayerListListener();

        // Set startbutton if host
        setStartButton();

        // Listen for game start
        setStartListener();
    }

    /**
     * Listen for the game to start
     */
    private void setStartListener() {
        handler.getFirebaseGameReference().child("started").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If the game is started, go to the next state
                if ((boolean) dataSnapshot.getValue()) nextState();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    @OnClick(R.id.startGameButton)
    protected void startGameButton() {
        // Only start the game if we are the host
        if (handler.isHost()) {
            handler.getFirebaseGameReference().child("started").setValue(true);
        }
    }

    @OnItemLongClick(R.id.playerListView)
    protected boolean onPlayerLongClick(final int position) {
        // Make sure that you can only kick someone else when you are the game host
        if (handler.isHost() && !playersList.get(position).get("id").equals(handler.getFirebaseAuthenticationData().getUid())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(handler.getActivityReference())
                    .setTitle("Kick player?")
                    .setMessage("Would you like to kick: " + playersList.get(position).get("nickname") + "?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handler.getFirebaseGameReference()
                                    .child("players")
                                    .child(playersList.get(position).get("id"))
                                    .removeValue();
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = builder.show();
        }

        // Need to return something due to ButterKnife
        return true;
    }

    /**
     * Populates game info textViews.
     */
    private void setGameInfo() {
        handler.getFirebaseGameReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Game game = dataSnapshot.getValue(Game.class);
                gameCodeTextView.setText("Game Code: " + game.getGameCode());
                if (game.getStarted())
                    startedTextView.setText(R.string.text_game_has_started);
                else
                    startedTextView.setText(R.string.text_game_not_started);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /**
     * Enables the start button if we are the game host, otherwise disable it for other players
     */
    private void setStartButton() {
        if (handler.isHost()) {
            // Set start button to enabled if we are the game host
            startGameButton.setEnabled(true);
        } else {
            // If we are not the game host, disable start button and change text
            startGameButton.setText(handler.getActivityReference().getString(R.string.btn_waiting_on_start));
        }
    }

    /**
     * Updates player list when players join or leave the game.
     */
    private void setPlayerListListener() {
        // Create an adapter to represent the playersList
        final SimpleAdapter simpleAdapter = new SimpleAdapter(handler.getActivityReference(),
                playersList,
                android.R.layout.simple_list_item_2,
                new String[] {"nickname", "role"},
                new int[] {android.R.id.text1, android.R.id.text2});
        // Assign adapter to the ListView
        playersListView.setAdapter(simpleAdapter);

        // Create a child event listener on the game object in Firebase to listen for changes in the players list
        handler.getFirebaseGameReference().child("players").addChildEventListener(new ChildEventListener() {
            // If player has joined
            @Override
            public void onChildAdded(final DataSnapshot playerInGameSnapshot, String s) {
                // Look up player in users reference
                handler.getFirebaseUsersReference().child(playerInGameSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot playerSnapshot) {
                        // Get the player object
                        Player player = playerSnapshot.getValue(Player.class);

                        // Create Map to represent player in the list
                        Map<String, String> playerItem = new HashMap<String, String>(3);
                        playerItem.put("id", playerSnapshot.getKey());
                        playerItem.put("nickname", player.getNickname());

                        // Put the specific role of the player
                        if (playerSnapshot.getKey().equals(handler.getFirebaseAuthenticationData().getUid()) && player.isGameHostInGame(handler.getFirebaseGameReference().getKey())) {
                            // Player is us and we are the game host
                            playerItem.put("role", "You are the Game Host");
                        } else if (playerSnapshot.getKey().equals(handler.getFirebaseAuthenticationData().getUid())) {
                            // Player is us
                            playerItem.put("role", "You");
                        } else if (player.isGameHostInGame(handler.getFirebaseGameReference().getKey())) {
                            // Other player is game host
                            playerItem.put("role", "Game Host");
                        }

                        // Add player to playersList list and notify
                        playersList.add(playerItem);
                        simpleAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }

            // If player is removed
            @Override
            public void onChildRemoved(final DataSnapshot playerInGameSnapshot) {
                handler.getFirebaseUsersReference().child(playerInGameSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot playerSnapshot) {
                        // Find player with the correct player Id to remove from the players list
                        for (int i = 0; i < playersList.toArray().length; i++) {
                            if (playersList.get(i).get("id").equals(playerSnapshot.getKey()))
                                playersList.remove(i);
                        }
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
