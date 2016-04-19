package com.tjuesyv.tjuesyv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.tjuesyv.tjuesyv.gameHandlers.GameObserver;
import com.tjuesyv.tjuesyv.ui.Prompter;

public class GameActivity extends AppCompatActivity {

    private GameObserver gameObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This multi-view layout starts with the lobby view
        setContentView(R.layout.activity_game);

        // Setup game
        gameObserver = new GameObserver(this);
        gameObserver.activate();
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
        gameObserver.close();
        // Exit server if host
        // Logout if user
    }
}