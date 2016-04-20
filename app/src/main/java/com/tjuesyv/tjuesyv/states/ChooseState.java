package com.tjuesyv.tjuesyv.states;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.GameActivity;
import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.gameHandlers.GameObserver;
import com.tjuesyv.tjuesyv.firebaseObjects.Game;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * In this state, the players get the statement/question on top and a list of options generated by the others players
 * The player is to choose one of these, and can afterwards vote on those he/she liked
 * The game master is just sent to the vote view at once
 * This stage should be timed
 */
public class ChooseState extends GameState {

    @Bind(R.id.chooseContinueButton) Button chooseContinueButton;
    @Bind(R.id.answersView) ListView answerListView;


    private static final int MAIN_VIEW = 3;
    private static final int WAITING_VIEW = 4;

    /**
     * Called once the state is entered
     *
     * @param observer
     */
    public ChooseState(GameObserver observer) {
        super(observer);

        // Setup ButterKnife
        ButterKnife.bind(this, observer.getActivityReference());
        if(observer.isHost()){
            setHostListView();
        }else {
            setAnswersListView();
        }

    }

    private void setHostListView() {
        final ArrayList<String> answersList= new ArrayList<>();
        final HostArrayAdapter<String> adapter = new HostArrayAdapter(observer.getActivityReference(),android.R.layout.simple_list_item_1,answersList);
        observer.getFirebaseGameReference().addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Game game=dataSnapshot.getValue(Game.class);
                observer.getFirebaseUsersReference().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (String key:game.getPlayers().keySet()
                                ) {

                            adapter.add(String.valueOf(dataSnapshot.child(key).child("nickname").getValue()));

                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
                answerListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public int getViewId() {
        return MAIN_VIEW;
    }

    private void setAnswersListView() {
        final ArrayList<String>answersList=new ArrayList<String>();

        answerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //TODO: fill array with answer data from ???
        //TODO: give points to the player with the selected answer

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(observer.getActivityReference(),android.R.layout.simple_list_item_single_choice,answersList);

        observer.getFirebaseGameReference().addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Game game=dataSnapshot.getValue(Game.class);
                observer.getFirebaseUsersReference().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (String key:game.getPlayers().keySet()
                                ) {

                            adapter.add(String.valueOf(dataSnapshot.child(key).child("nickname").getValue()));

                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
                answerListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @OnClick(R.id.chooseContinueButton)
    protected void goToScore() {
        processAnswer(answerListView.getCheckedItemPosition());

        nextState();
    }

    private void processAnswer(int selectionPos) {
        //TODO: more processing of answers here

    }

    private class HostArrayAdapter<T> extends ArrayAdapter {
        public HostArrayAdapter(GameActivity activityReference, int simple_list_item_single_choice, ArrayList<String> answersList) {
            super(activityReference,simple_list_item_single_choice,answersList);
        }
        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }
}
