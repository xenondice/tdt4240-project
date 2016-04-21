package com.tjuesyv.tjuesyv.states;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;

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

        // Sets a listener for the scores of the players
        setScoreListListener();
    }

    @Override
    public int getViewId() {
        return MAIN_VIEW;
    }

    @OnClick(R.id.scoreContinueButton)
    protected void goToNextRound() {
        nextState();
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
        observer.getFirebaseGameReference().child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot playerIdSnapshot) {
                // We get a list of players as a Map
                List<String> playerIds = (List<String>) playerIdSnapshot.getValue();
                // Iterate over the players in the game
                for (final String playerId : playerIds) {
                    // Lookup players by their playerId
                    observer.getFirebaseUsersReference().child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot playerSnapshot) {
                            // Get the Player object
                            final Player player = playerSnapshot.getValue(Player.class);

                            // Find scores of player
                            observer.getFirebaseScoresReference().child(observer.getFirebaseGameReference().getKey()).child(playerSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot scoreSnapshot) {
                                    // Get the Score object
                                    Score score = scoreSnapshot.getValue(Score.class);

                                    // Add player and their score to adapter
                                    adapter.add(player.getNickname() + " - " + score.getScore());
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                }
                            });
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    });

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }
}
