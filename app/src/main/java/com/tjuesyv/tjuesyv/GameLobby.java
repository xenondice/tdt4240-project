package com.tjuesyv.tjuesyv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class GameLobby extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_lobby);

        // Get Game ID
        Intent intent = getIntent();
        String gameID = intent.getStringExtra("gameID");

        // Set Game Info Text
        setTitle("Game: " + gameID);
    }
}
