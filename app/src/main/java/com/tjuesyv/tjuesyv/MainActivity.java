package com.tjuesyv.tjuesyv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import org.hashids.Hashids;

public class MainActivity extends AppCompatActivity {

    private EditText gameCodeText;
    private EditText nameText;
    private Button createGameButton;
    private Button joinGameButton;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Game Code EditText
        gameCodeText = (EditText) findViewById(R.id.gameCodeText);
        gameCodeText.setFilters(new InputFilter[] { new InputFilter.AllCaps(), new InputFilter.LengthFilter(4)});

        // Nick name EditText
        nameText = (EditText) findViewById(R.id.nameText);

        // Get Button object
        createGameButton = (Button) findViewById(R.id.createGameButton);

        // Get Button object
        joinGameButton = (Button) findViewById(R.id.joinGameButton);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.listView);
        // Create a new Adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);
        // Assign adapter to ListView
        listView.setAdapter(adapter);
        // Use Firebase to populate the list.
        Firebase.setAndroidContext(this);
        final Firebase ref = new Firebase(Constants.FIREBASE_URL);
        final Firebase gamesRef = ref.child("games");

        gamesRef.addChildEventListener(new ChildEventListener() {
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        adapter.add((String) dataSnapshot.child("gameID").getValue());
                    }
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        adapter.remove((String) dataSnapshot.child("gameID").getValue());
                    }
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });

        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGame(gamesRef);
            }
        });
    }

    private void createNewGame(Firebase gamesRef) {
        Firebase newGameRef = gamesRef.push();
        Hashids hashids = new Hashids(newGameRef.getKey(), 4, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        String gameID = hashids.encode(1);
        newGameRef.setValue(new Game(gameID));

        Intent intent = new Intent(this, GameLobby.class);
        intent.putExtra("gameID", gameID);
        startActivity(intent);
    }
}
