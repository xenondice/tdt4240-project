package com.tjuesyv.tjuesyv.states;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.GameActivity;
import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.firebaseObjects.Player;
import com.tjuesyv.tjuesyv.firebaseObjects.Question;
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
    @Bind(R.id.chooseSubmitButton) Button chooseSubmitButton;
    @Bind(R.id.answersView) ListView answerListView;
    @Bind(R.id.chooseGameMasterListView) ListView masterAnswerListView;
    @Bind(R.id.textWhoIsMasterChoose) TextView textWhoIsMaster;

    private static final int MAIN_VIEW = 3;
    private static final int WAITING_VIEW = 4;

    private SimpleAdapter adapter;

    /**
     * Called once the state is entered
     *
     * @param observer
     */
    public ChooseState(GameObserver observer) {
        super(observer);

        // Setup ButterKnife
        ButterKnife.bind(this, observer.getActivityReference());
        if(observer.isGameMaster()){
            setMasterListView();
            chooseContinueButton.setEnabled(true);
        }else {

            textWhoIsMaster.setText("Current Game Master: "+observer.getPlayerFromId(observer.getGameInfo().getGameMaster()).getNickname());
            setAnswersListView();
        }

    }

    private void setMasterListView() {
        masterAnswerListView.setAdapter(adapter);
        observer.getFirebaseGameReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final Game game = dataSnapshot.getValue(Game.class);
                observer.getFirebaseAnswersReference().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

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

    @Override
    public int getViewId() {
        if (observer.isGameMaster()) 
            return WAITING_VIEW;
        else
            return MAIN_VIEW;
    }

    private void setAnswersListView() {
        final ArrayList<String>answersList=new ArrayList<String>();

        answerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //TODO: give points to the player with the selected answer

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(observer.getActivityReference(),android.R.layout.simple_list_item_single_choice,answersList);

        setList(adapter);

    }
    private int randomInt(){
        int random = (int) Math.floor(Math.random() * observer.getGameInfo().getAnswers().size());
        return random;
    }

    private void setList(final ArrayAdapter<String> adapter) {
        String correctAnswerKey = String.valueOf(observer.getGameInfo().getQuestion());

        //get the correct answer
        Question question = observer.getQuestion();
        adapter.add(question.getAnswer());

        //populate ListView with answers from players
        for (String key:observer.getGameInfo().getPlayers()) {
            String temp = null;
            //Must check that the uID for game master is skipped, because answer will be null.
            if (!key.equals(observer.getGameInfo().getGameMaster())) {
                temp = observer.getGameInfo().getAnswers().get(key);
                adapter.add(temp);
            }
        }
        //TODO: Random sort listview.
        answerListView.setAdapter(adapter);
    }

    @OnClick(R.id.chooseContinueButton)
    protected void goToScore() {
       //TODO: Make shure all players have answered before calling nextState()
        chooseContinueButton.setEnabled(false);
        nextState();
    }
    @OnClick(R.id.chooseSubmitButton)
    protected void submitChoice() {
        //TODO: Submit choice to firebase
        chooseSubmitButton.setEnabled(false);
        chooseSubmitButton.setText("Waiting for Game Master");
    }


}
