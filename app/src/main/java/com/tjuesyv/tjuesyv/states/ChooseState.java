package com.tjuesyv.tjuesyv.states;

import android.widget.Button;

import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by RayTM on 08.04.2016.
 * In this state, the players get the statement/question on top and a list of options generated by the others players
 * The player is to choose one of these, and can afterwards vote on those he/she liked
 * The game master is just sent to the vote view at once
 * This stage should be timed
 */
public class ChooseState extends GameState {

    @Bind(R.id.chooseContinueButton) Button chooseContinueButton;

    private static final int MAIN_VIEW = 3;
    private static final int WAITING_VIEW = 4;

    @Override
    public int getViewId() {
        return MAIN_VIEW;
    }

    @Override
    public void onEnter() {
        // Setup ButterKnife
        ButterKnife.bind(this, handler.getActivityReference());
    }

    @OnClick(R.id.chooseContinueButton)
    protected void goToScore() {
        nextState();
    }
}
