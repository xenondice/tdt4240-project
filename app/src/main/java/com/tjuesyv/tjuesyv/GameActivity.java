package com.tjuesyv.tjuesyv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.firebaseObjects.Game;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GameActivity extends AppCompatActivity {

    @Bind(R.id.rootFlipper) ViewFlipper rootFlipper;
    @Bind(R.id.gameCodeTextView) TextView gameCodeTextView;
    @Bind(R.id.startedTextView) TextView startedTextView;
    @Bind(R.id.activeTextView) TextView activeTextView;
    @Bind(R.id.startGameButton) Button startGameButton;
    @Bind(R.id.playerListView) ListView playersListView;

    private String gameUID;
    //private StateHandler

    private Firebase rootRef;
    private Firebase gamesRef;
    private Firebase usersRef;
    private Firebase currentGameRef;
    private Firebase currentUserRef;
    private AuthData authData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup action
        init();

        //while ()

        // Displays game info
        setGameInfo();

        // Displays players in a listView
        setPlayerList();
    }

    private void init() {
        setContentView(R.layout.activity_game);

        // Setup ButterKnife
        ButterKnife.bind(this);

        // Get intent
        Intent intent = getIntent();
        gameUID = intent.getStringExtra("GAME_UID");

        // Create main Firebase ref
        rootRef = new Firebase(getResources().getString(R.string.firebase_url));

        // Get Firebase authentication
        authData = rootRef.getAuth();

        // Setup other Firebase references
        gamesRef = rootRef.child("games");
        usersRef = rootRef.child("users");
        currentGameRef = gamesRef.child(gameUID);
        currentUserRef = usersRef.child(authData.getUid());
    }

    @OnClick(R.id.startGameButton)
    protected void startGameButton() {
        startGame();
    }

    /**
     * Populates game info textViews.
     */
    private void setGameInfo() {
        currentGameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Game game = dataSnapshot.getValue(Game.class);
                gameCodeTextView.setText("Game code: " + game.getGameCode());
                startedTextView.setText("Started: " + game.getStarted());
                activeTextView.setText("Active: " + game.getActive());
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
        // Create a new Adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, android.R.id.text1);
        // Assign adapter to ListView
        playersListView.setAdapter(adapter);

        // Set child listener for the current games players
        currentGameRef.child("players").addChildEventListener(new ChildEventListener() {
            // If player is added
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                usersRef.child(dataSnapshot.getKey()).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        adapter.add(dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }

            // If player is removed
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                usersRef.child(dataSnapshot.getKey()).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        adapter.remove(dataSnapshot.getValue().toString());
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

    /**
     * Sets the started field of the current game to true.
     */
    private void startGame() {
        currentGameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Game game = dataSnapshot.getValue(Game.class);
                if (game.getGameHost().equals(authData.getUid()))
                    currentGameRef.child("started").setValue(true);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            reallyExit();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void reallyExit() {
        new Prompter(getText(R.string.prompt_exit), this) {
            @Override
            public void callBack(boolean answer) {
                if (answer) finish();
            }
        }.ask();
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}