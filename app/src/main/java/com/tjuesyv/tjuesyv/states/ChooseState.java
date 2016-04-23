package com.tjuesyv.tjuesyv.states;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.GameActivity;
import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.firebaseObjects.Player;
import com.tjuesyv.tjuesyv.firebaseObjects.Question;
import com.tjuesyv.tjuesyv.firebaseObjects.Score;
import com.tjuesyv.tjuesyv.gameHandlers.GameObserver;
import com.tjuesyv.tjuesyv.firebaseObjects.Game;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<Map<String, Object>> answersList = new ArrayList<>();

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
            answerListView.setEnabled(true);
            chooseSubmitButton.setEnabled(true);
            chooseSubmitButton.setText("Submit");
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
        final SimpleAdapter answersAdapter = new SimpleAdapter(observer.getActivityReference(),
                answersList,
                android.R.layout.simple_list_item_single_choice,
                new String[] {"answer"},
                new int[] {android.R.id.text1});

        answerListView.setAdapter(answersAdapter);
        answerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        String correctAnswerKey = String.valueOf(observer.getGameInfo().getQuestion());

        //get the correct answer
        Question question = observer.getQuestion();
        Map<String, Object> correctAnswerItem = new HashMap<String, Object>();
        correctAnswerItem.put("answer", question.getAnswer());
        correctAnswerItem.put("correct", true);
        answersList.add(correctAnswerItem);
        answersAdapter.notifyDataSetChanged();

        //populate ListView with answers from players
        for (Map.Entry<String, String> answer : observer.getGameInfo().getAnswers().entrySet()) {
            // Hide correct answers from players
            if (observer.getGameInfo().getCorrectAnswers() != null) {
                if (observer.getGameInfo().getCorrectAnswers().containsKey(answer.getKey())) {
                    continue;
                }
            }
                Map<String, Object> answerItem = new HashMap<String, Object>();
                answerItem.put("answer", answer.getValue());
                answerItem.put("playerId", answer.getKey());
                answersList.add(answerItem);
        }
        answersAdapter.notifyDataSetChanged();
    }

    private int randomInt(){
        int random = (int) Math.floor(Math.random() * observer.getGameInfo().getAnswers().size());
        return random;
    }

    @OnClick(R.id.chooseContinueButton)
    protected void goToScore() {

        nextState();
    }

    private void processAnswer(int selectionPos) {
        Map<String, Object> answer = answersList.get(selectionPos);

        // Chose the correct pre made answer
        if (answer.containsKey("correct")) {
            Score currentScore = observer.getScoreForPlayer(observer.getFirebaseAuthenticationData().getUid());
            currentScore.incrementScore(1);
            observer.getFirebaseScoresReference().child(observer.getFirebaseGameReference().getKey()).child(observer.getFirebaseAuthenticationData().getUid()).setValue(currentScore);
        }
        // Chose wrong answer, give points to other player
        else {
            String playerId = answer.get("playerId").toString();
            Score currentScore = observer.getScoreForPlayer(playerId);
            currentScore.incrementScore(1);
            observer.getFirebaseScoresReference().child(observer.getFirebaseGameReference().getKey()).child(playerId).setValue(currentScore);
        }
    }
    @OnClick(R.id.chooseSubmitButton)
    protected void submitChoice() {
        //TODO: fix dette
        if (answerListView.getSelectedItemPosition() >= 0){
            processAnswer(answerListView.getCheckedItemPosition());
            answerListView.setEnabled(false);
            chooseSubmitButton.setEnabled(false);
            chooseSubmitButton.setText("Waiting for Game Master");
        }else{
            Toast.makeText(observer.getActivityReference(), "Nothing selected! Please select an answer.", Toast.LENGTH_LONG).show();
        }

    }

}
