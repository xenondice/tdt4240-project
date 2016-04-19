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
import com.tjuesyv.tjuesyv.firebaseObjects.Score;
import com.tjuesyv.tjuesyv.gameHandlers.GameObserver;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;
import com.tjuesyv.tjuesyv.gameModes.DefaultMode;

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
        roundTextField.setText(observer.getCurrentRound()+" of "+ DefaultMode.NUMBER_OF_ROUNDS);
        setScoresListView();

    }

    @Override
    public int getViewId() {
        return MAIN_VIEW;
    }

    @OnClick(R.id.scoreContinueButton)
    protected void goToNextRound() {
        nextState();
    }

    private void setScoresListView(){
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(observer.getActivityReference(),android.R.layout.simple_list_item_1, android.R.id.text1);

        scoresListView.setAdapter(adapter);

        observer.getFirebaseGameReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               final Game game = dataSnapshot.getValue(Game.class);
                final Score score= dataSnapshot.getValue(Score.class);

                observer.getFirebaseUsersReference().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (String key : game.getPlayers().keySet()
                                ) {
                            String temp = "";
                            Log.d("Scores", key);
                            Log.d("Scores", String.valueOf(score.getScore()));

                            temp += String.valueOf(dataSnapshot.child(key).child("nickname").getValue());
                            Log.d("Scores", temp);
                            temp += " : ";
                            temp += String.valueOf(score.getScore());
                            Log.d("Scores", temp);
                            adapter.add(temp);

                        }
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
        //adapter.add("Vegar : 6");

    }
}
