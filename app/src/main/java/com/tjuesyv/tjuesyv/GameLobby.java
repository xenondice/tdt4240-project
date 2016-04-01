package com.tjuesyv.tjuesyv;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.tjuesyv.tjuesyv.firebaseObjects.Game;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GameLobby extends AppCompatActivity{

    @Bind(R.id.startGameButton) Button startGameButton;
    @Bind(R.id.listView) ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_lobby);

        // Setup ButterKnife and Firebase
        ButterKnife.bind(this);
        Firebase.setAndroidContext(this);

        Intent intent = getIntent();
        String gameCode = intent.getStringExtra("GAME_CODE");
        String nickname = intent.getStringExtra("PLAYER");
        Snackbar.make(findViewById(R.id.rootView),
                "Joined game: " + gameCode + " as user: " + nickname,
                Snackbar.LENGTH_LONG)
                .show();


        // Create a new Adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);
        // Assign adapter to ListView
        listView.setAdapter(adapter);

        final Firebase ref = new Firebase(Constants.FIREBASE_URL).child("games");
        Query queryRef = ref.orderByChild("gameCode").equalTo(gameCode);

        queryRef.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Game game = dataSnapshot.getValue(Game.class);
                //adapter.add((String)dataSnapshot.child("gameCode").getValue());
                adapter.add("GameCode: " + game.getGameCode());
                adapter.add("Key: " + dataSnapshot.getKey());
                adapter.add("CreatedAt: " + game.getCreatedAtLong().toString());
            }
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.remove((String)dataSnapshot.child("gameCode").getValue());
            }
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            public void onCancelled(FirebaseError firebaseError) { }
        });
    }

}
