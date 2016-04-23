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
import com.tjuesyv.tjuesyv.gameHandlers.GameObserver;
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
    private final List<Map<String, String>> playersList = new ArrayList<>();
    private SimpleAdapter simpleAdapter;

    @Override
    public int getViewId() {
        return MAIN_VIEW;
    }

    public LobbyState(GameObserver observer) {
        super(observer);

        // Setup ButterKnife
        ButterKnife.bind(this, this.observer.getActivityReference());

        // Displays game info
        setGameInfo();

        // Set startbutton if host
        setStartButton();
    }

    @OnClick(R.id.startGameButton)
    protected void startGameButton() {
        if (observer.isHost()) nextState();
    }

    @OnItemLongClick(R.id.playerListView)
    protected boolean onPlayerLongClick(final int position) {
        // Make sure that you can only kick someone else when you are the game host
        if (observer.isHost() && !playersList.get(position).get("id").equals(observer.getFirebaseAuthenticationData().getUid())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(observer.getActivityReference())
                    .setTitle("Kick player?")
                    .setMessage("Would you like to kick: " + playersList.get(position).get("nickname") + "?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            observer.getFirebaseGameReference()
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
        gameCodeTextView.setText(getGameInfo().getGameCode());
        startedTextView.setText(getGameInfo().getStarted()?
                R.string.text_game_has_started:
                R.string.text_game_not_started);
        // Create an adapter to represent the playersList
        simpleAdapter = new SimpleAdapter(observer.getActivityReference(),
                playersList,
                android.R.layout.simple_list_item_2,
                new String[]{"nickname", "role"},
                new int[]{android.R.id.text1, android.R.id.text2});
        // Assign adapter to the ListView
        playersListView.setAdapter(simpleAdapter);
        // Fill with existing players
        for (String playerId : observer.getActivePlayers()) {
            putPlayerInList(playerId);
        }
    }

    /**
     * Enables the start button if we are the game host, otherwise disable it for other players
     */
    private void setStartButton() {
        if (observer.isHost()) {
            // Set start button to enabled if we are the game host
            startGameButton.setEnabled(true);
        } else {
            // If we are not the game host, disable start button and change text
            startGameButton.setText(observer.getActivityReference().getString(R.string.btn_waiting_on_start));
        }
    }

    @Override
    public void newPlayerJoined(String playerId) {
        putPlayerInList(playerId);
    }

    private void putPlayerInList(String playerId) {
        // Get the player object
        Player player = observer.getPlayerFromId(playerId);
        System.out.println(player);

        // Create list to represent player in the list
        Map<String, String> playerItem = new HashMap<String, String>(3);
        playerItem.put("id", playerId);
        playerItem.put("nickname", player.getNickname());

        // Put the specific role of the player
        if (playerId.equals(observer.getFirebaseAuthenticationData().getUid()) && player.isGameHostInGame(observer.getFirebaseGameReference().getKey())) {
            // Player is us and we are the game host
            playerItem.put("role", "You are the Game Host");
        } else if (playerId.equals(observer.getFirebaseAuthenticationData().getUid())) {
            // Player is us
            playerItem.put("role", "You");
        } else if (player.isGameHostInGame(observer.getFirebaseGameReference().getKey())) {
            // Other player is game host
            playerItem.put("role", "Game Host");
        }

        // Add player to playersList list and notify
        playersList.add(playerItem);
        simpleAdapter.notifyDataSetChanged();
    }

    // If player is removed
    @Override
    public void playerLeft(String playerId) {
        // Find player with the correct player Id to remove from the players list
        for (int i = 0; i < playersList.toArray().length; i++) {
            if (playersList.get(i).get("id").equals(playerId))
                playersList.remove(i);
        }
        simpleAdapter.notifyDataSetChanged();
    }
}
