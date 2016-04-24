package com.tjuesyv.tjuesyv.states;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.firebaseObjects.Game;
import com.tjuesyv.tjuesyv.firebaseObjects.Player;
import com.tjuesyv.tjuesyv.firebaseObjects.Score;
import com.tjuesyv.tjuesyv.gameHandlers.GameObserver;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;
import com.tjuesyv.tjuesyv.gameModes.DefaultMode;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Here all players are faced with a fancy and exciting representation of the score so far into the game
 * Should also show a slightly different view if it's the final game
 */
public class ScoreState extends GameState {

    @Bind(R.id.scoreContinueButton) Button scoreContinueButton;
    @Bind(R.id.roundTextField) TextView roundTextField;
    @Bind(R.id.scoresListView) ListView scoresListView;

    private static final int MAIN_VIEW = 5;

    /**
     * Called once the state is entered
     *
     * @param observer
     */
    public ScoreState(GameObserver observer) {
        super(observer);

        // Setup ButterKnife
        ButterKnife.bind(this, observer.getActivityReference());

        roundTextField.setText(observer.getCurrentRound() + " of " + DefaultMode.NUMBER_OF_ROUNDS);

        if (observer.isGameMaster()) {
            scoreContinueButton.setEnabled(true);
            scoreContinueButton.setText("Continue");
        }
        else {
            scoreContinueButton.setEnabled(false);
            scoreContinueButton.setText("Waiting for Game Master");
        }

        // Sets a listener for the scores of the players
        setScoreListListener();
    }

    @Override
    public int getViewId() {
        return MAIN_VIEW;
    }

    @OnClick(R.id.scoreContinueButton)
    protected void goToNextRound() {

        if (observer.isGameMaster()) {
            nextState();
        }else{
            Toast.makeText(observer.getActivityReference(), "Waiting for Game Master...",Toast.LENGTH_LONG).show();
            scoreContinueButton.setEnabled(false);
            scoreContinueButton.setText("Waiting for Game Master...");
        }

    }

    /**
     * Populates the scores of the players
     */
    private void setScoreListListener(){
        // Create an adapter for the list of players scores
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(observer.getActivityReference(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1);
        // Assign adapter to the ListView
        scoresListView.setAdapter(adapter);

        // Need to lookup players in game, the nicknames of the players and their scores
        // Get the players that are in the current game
        for (String playerId : observer.getActivePlayers()) {
            Player player = observer.getPlayerFromId(playerId);
            Score score = observer.getScoreForPlayer(playerId);
            adapter.add(player.getNickname() + " - " + score.getScore());
        }
    }
}
