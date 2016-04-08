package com.tjuesyv.tjuesyv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.tjuesyv.tjuesyv.gameHandlers.GameHandler;
import com.tjuesyv.tjuesyv.gameModes.DefaultMode;
import com.tjuesyv.tjuesyv.ui.Prompter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GameActivity extends AppCompatActivity {

    private GameHandler gameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        // Setup game
        gameHandler = new GameHandler(this, new DefaultMode());

        // Start game
        gameHandler.startGame();
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

    @Override
    public void finish() {
        super.finish();
        // Exit server if host
        // Logout if user
    }
}