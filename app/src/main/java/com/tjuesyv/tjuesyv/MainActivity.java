package com.tjuesyv.tjuesyv;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.firebaseObjects.Game;
import com.tjuesyv.tjuesyv.firebaseObjects.Player;

import org.hashids.Hashids;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.createGameButton) Button createGameButton;
    @Bind(R.id.joinGameButton) Button joinGameButton;
    @Bind(R.id.gameCodeText) EditText gameCodeText;
    @Bind(R.id.nicknameText) EditText nicknameText;
    @Bind(R.id.nicknameTextInputLayout) TextInputLayout nicknameTextInputLayout;
    @Bind(R.id.gameCodeTextInputLayout) TextInputLayout gameCodeTextInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup ButterKnife and Firebase
        ButterKnife.bind(this);
        Firebase.setAndroidContext(this);
    }

    @OnTextChanged(R.id.gameCodeText)
    protected void validGameCode(CharSequence text) {
        isValidGameCode(text.toString());
    }

    @OnTextChanged(R.id.nicknameText)
    protected void validNickname(CharSequence text) {
        isValidNickname(text.toString());
    }

    @OnClick(R.id.createGameButton)
    protected void createGame() {
        String nickname = nicknameText.getText().toString();
        // Validate nickname
        if (!isValidNickname(nickname))
            return;

        // Create new player
        Player newPlayer = createPlayer(nickname);

        // Create reference to new game entry
        Firebase newGameRef = new Firebase(Constants.FIREBASE_URL).child("games").push();
        // Use the Firebase generated UID as salt to generate 4 letter/digit code
        String gameCode = createGameCode(newGameRef.getKey());
        // Populate the game entry in Firebase with the game object
        Game newGame = new Game(gameCode);
        newGame.addPlayer(newPlayer);
        newGameRef.setValue(newGame);

        // Join game
        joinGameLobby(gameCode, newPlayer);
    }

    private Player createPlayer(final String nickname) {
        final Firebase ref = new Firebase(Constants.FIREBASE_URL);
        final Player player = new Player(nickname);
        ref.authAnonymously(new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                ref.child("users").child(authData.getUid()).setValue(player);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Snackbar.make(findViewById(R.id.rootView),
                        "Error signing in player: " + firebaseError.toString(),
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        });
        return player;
    }

    private void joinGameLobby(String gameCode, Player player) {
        Intent intent = new Intent(this, GameLobby.class);
        intent.putExtra("GAME_CODE", gameCode);
        intent.putExtra("PLAYER", player.getName());
        startActivity(intent);
    }


    @OnClick(R.id.joinGameButton)
    protected void joinGame() {
        // Validate nickname
        if (!isValidNickname(nicknameText.getText().toString()))
            return;

        // Validate game code
        if (!isValidGameCode(gameCodeText.getText().toString())) {
            return;
        }

        if (!isValidGame(gameCodeText.getText().toString()))
            return;

        // Create new player
        Player player = createPlayer(nicknameText.getText().toString());

        // Join game lobby
        joinGameLobby(gameCodeText.getText().toString(), player);
    }

    private boolean isValidGameCode(String gameCode) {
        if (gameCode.isEmpty()) {
            gameCodeTextInputLayout.setError(getString(R.string.error_empty_game_code));
            return false;
        } else if (gameCode.length() > 0 && gameCode.length() < 4) {
            gameCodeTextInputLayout.setError(getString(R.string.error_short_game_code));
            return false;
        } else {
            gameCodeTextInputLayout.setErrorEnabled(false);
            gameCodeTextInputLayout.setError(null);
        }
        return true;
    }

    private boolean isValidNickname(String nickname) {
        if (nickname.isEmpty()) {
            nicknameTextInputLayout.setError(getString(R.string.error_empty_nickname));
            return false;
        } else {
            nicknameTextInputLayout.setErrorEnabled(false);
            nicknameTextInputLayout.setError(null);
        }
        return true;
    }

    private boolean isValidGame(String gameCode) {
        final Firebase ref = new Firebase(Constants.FIREBASE_URL).child("games");
        Query queryRef = ref.orderByChild("gameCode").equalTo(gameCodeText.getText().toString());
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });

        return true;
    }

    private String createGameCode(String key) {
        Hashids hashids = new Hashids(key, 4, getString(R.string.edit_valid_game_codes));
        return hashids.encode(1);
    }
}
