package com.tjuesyv.tjuesyv.states;

import android.widget.Button;

import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by RayTM on 08.04.2016.
 * In this stage, the players get the statement/question and get some time to write a lie
 * After finishing, they press submit and is faced with a waiting screen
 * The game master also has the statement/question on top, but also a list of answers from
 * the players that is updated as they are submitted
 * The master can then mark duplicates and award points to those who wrote the correct answer
 * After finishing, the host can press next
 */
public class CreateState extends GameState {

    @Bind(R.id.createContinueButton) Button createContinueButton;

    private  static final int PLAYER_VIEW = 1;
    private  static final int GAME_MASTER_VIEW = 2;

    @Override
    public int getViewId() {
        return PLAYER_VIEW;
    }

    @Override
    public void onEnter() {
        // Setup ButterKnife
        ButterKnife.bind(this, handler.getActivityReference());
    }

    @OnClick(R.id.createContinueButton)
    protected void goToChoose() {
        nextState();
    }
}
