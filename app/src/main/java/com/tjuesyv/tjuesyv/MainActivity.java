package com.tjuesyv.tjuesyv;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
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
    protected void createGameButton() {
        // Validate nickname
        String nickname = nicknameText.getText().toString();
        if (isValidNickname(nickname)) {
            String gameCode = createGame(nickname);
            joinGame(gameCode, nickname);
        }
    }

    @OnClick(R.id.joinGameButton)
    protected void joinGameButton() {
        // Validate nickname and game code formatting
        String gameCode = gameCodeText.getText().toString();
        String nickname = nicknameText.getText().toString();
        if (isValidNickname(nickname) && isValidGameCode(gameCode))
            joinGame(gameCode, nickname);
    }

    private String createGame(String nickname) {
        // Create reference to new game entry
        Firebase newGameRef = new Firebase(Constants.FIREBASE_URL).child("games").push();
        // Use the Firebase generated UID as salt to generate 4 letter/digit code
        String gameCode = createGameCode(newGameRef.getKey());
        // Populate the game entry in Firebase with the game object
        Game newGame = new Game(gameCode);
        newGameRef.setValue(newGame);
        return gameCode;
    }

    private void joinGame(final String gameCode, final String nickname) {
        // Make sure the game is active and not full
        Firebase ref = new Firebase(Constants.FIREBASE_URL);
        Firebase gamesRef = ref.child("games");

        // Find games with the game code
        Query queryRef = gamesRef.orderByChild("gameCode").equalTo(gameCode);
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Game game = dataSnapshot.getValue(Game.class);
                if (game.getActive()) {
                    // Join lobby as new player
                    goToGameLobby(gameCode, createPlayer(nickname));
                    // Clear any error messages
                    gameCodeTextInputLayout.setErrorEnabled(false);
                    gameCodeTextInputLayout.setError(null);
                }
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Snackbar.make(findViewById(R.id.rootView),
                        "Error joining game: " + gameCode + ". Error: " + firebaseError.toString(),
                        Snackbar.LENGTH_LONG).show();
            }
        });

        // Display message if the query results with nothing
        gameCodeTextInputLayout.setError(getString(R.string.error_no_active_game_found));
    }

    private Player createPlayer(String nickname) {
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
                        Snackbar.LENGTH_LONG).show();
            }
        });
        return player;
    }

    private void goToGameLobby(String gameCode, Player player) {
        Intent intent = new Intent(this, GameLobby.class);
        intent.putExtra("GAME_CODE", gameCode);
        intent.putExtra("PLAYER", player.getName());
        startActivity(intent);
        overridePendingTransition(0, 0);
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

    private String createGameCode(String key) {
        Hashids hashids = new Hashids(key, 4, getString(R.string.edit_valid_game_codes));
        return hashids.encode(1);
    }
}
