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
import com.tjuesyv.tjuesyv.gameHandlers.GameState;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by RayTM on 08.04.2016.
 * Here all players are faced with a fancy and exciting representation of the score so far into the game
 * Should also show a slightly different view if it's the final game
 */
public class ScoreState extends GameState {

    @Bind(R.id.scoreContinueButton) Button scoreContinueButton;
    @Bind(R.id.roundTextField) TextView roundTextField;
    @Bind(R.id.scoresListView) ListView scoresListView;

    private static final int MAIN_VIEW = 5;

    @Override
    public int getViewId() {
        return MAIN_VIEW;
    }

    @Override
    public void onEnter() {
        // Setup ButterKnife
        ButterKnife.bind(this, handler.getActivityReference());
        roundTextField.setText(handler.getCurrentRound()+1+" of "+handler.getNumberOfRounds());
        setScoresListView();
    }

    @OnClick(R.id.scoreContinueButton)
    protected void goToNextRound() {
        if (handler.getCurrentRound() + 1 == handler.getNumberOfRounds())
            if (handler.isHost()) handler.getFirebaseGameReference().child("started").setValue(false);
        nextState();
    }

    private void setScoresListView(){
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(handler.getActivityReference(),android.R.layout.simple_list_item_1, android.R.id.text1);

        scoresListView.setAdapter(adapter);

        handler.getFirebaseGameReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               final Game game = dataSnapshot.getValue(Game.class);
                final Score score= dataSnapshot.getValue(Score.class);

                handler.getFirebaseUsersReference().addListenerForSingleValueEvent(new ValueEventListener() {
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
