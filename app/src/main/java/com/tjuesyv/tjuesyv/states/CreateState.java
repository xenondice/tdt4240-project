package com.tjuesyv.tjuesyv.states;

import android.support.design.widget.TextInputLayout;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.firebaseObjects.Player;
import com.tjuesyv.tjuesyv.firebaseObjects.Question;
import com.tjuesyv.tjuesyv.gameHandlers.GameObserver;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * In this stage, the players get the statement/question and get some time to write a lie
 * After finishing, they press submit and is faced with a waiting screen
 * The game master also has the statement/question on top, but also a list of answers from
 * the players that is updated as they are submitted
 * The master can then mark duplicates and award points to those who wrote the correct answer
 * After finishing, the host can press next
 */
public class CreateState extends GameState {

    @Bind(R.id.createSubmitButton) Button createSubmitButton;
    @Bind(R.id.createContinueButton) Button createContinueButton;
    @Bind(R.id.questionPlayerTextView) TextView questionPlayerTextView;
    @Bind(R.id.questionGameMasterTextView) TextView questionGameMasterTextView;
    @Bind(R.id.answerTextView) TextView answerTextView;
    @Bind(R.id.answerEditText) EditText answerEditText;
    @Bind(R.id.answerTextInputLayout) TextInputLayout answerTextInputLayout;
    @Bind(R.id.answersGameMasterListView) ListView answersGameMasterListView;

    private static final int PLAYER_VIEW = 1;
    private static final int GAME_MASTER_VIEW = 2;

    private List<Map<String, Object>> answersList = new ArrayList<>();

    /**
     * Called once the state is entered
     *
     * @param observer
     */
    public CreateState(GameObserver observer) {
        super(observer);

        // Setup ButterKnife
        ButterKnife.bind(this, observer.getActivityReference());

        // Clear fields and buttons
        answerTextInputLayout.setVisibility(View.VISIBLE);
        createSubmitButton.setEnabled(true);
        createSubmitButton.setText(observer.getActivityReference().getString(R.string.btn_submit_answer));

        // Get the question for this round
        getQuestion();

        if(observer.isGameMaster()){
            setMasterListView();
        }
    }

    private void setMasterListView() {
        final SimpleAdapter answersAdapter = new SimpleAdapter(observer.getActivityReference(),
                answersList,
                android.R.layout.simple_list_item_multiple_choice,
                new String[] {"answer"},
                new int[] {android.R.id.text1});

        // Clear from previous round
        answersList.clear();
        observer.getFirebaseAnswersReference().setValue(null);
        observer.getFirebaseGameReference().child("correctAnswers").setValue(null);

        // Set adapter
        answersGameMasterListView.setAdapter(answersAdapter);
        answersGameMasterListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //TODO: give points to the player with the selected answer

        observer.getFirebaseAnswersReference().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final String answer = (String) dataSnapshot.getValue();
                final String playerKey = dataSnapshot.getKey();
                observer.getFirebaseUsersReference().child(playerKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Player player = dataSnapshot.getValue(Player.class);

                        Map<String, Object> answerItem = new HashMap<String, Object>();
                        answerItem.put("nickname", player.getNickname());
                        answerItem.put("playerId", playerKey);
                        answerItem.put("answer", answer);

                        answersList.add(answerItem);
                        answersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    @Override
    public int getViewId() {
        if (observer.isGameMaster())
            return GAME_MASTER_VIEW;
        else
            return PLAYER_VIEW;
    }

    @OnTextChanged(R.id.answerEditText)
    protected void validAnswer(CharSequence input) {
        isValidAnswer(input.toString());
    }

    @OnClick(R.id.createContinueButton)
    protected void continueButton(){

        int numberOfPlayers = observer.getGameInfo().getPlayers().size();
        if (observer.getGameInfo().getAnswers() != null) {
            if (observer.getGameInfo().getAnswers().size() == numberOfPlayers - 1){
                // Score correct answers
                scoreCorrectAnswers();
                // Go to next state
                nextState();
            }else{
                Toast.makeText(observer.getActivityReference(), "Missing a few answers.. Nag the players!", Toast.LENGTH_LONG).show();
            }

        }else{
            Toast.makeText(observer.getActivityReference(), "No answers submitted :(", Toast.LENGTH_LONG).show();
        }
    }

    private void scoreCorrectAnswers() {
        SparseBooleanArray checked = answersGameMasterListView.getCheckedItemPositions();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.get(i)) {
                Map<String, Object> answerItem = answersList.get(i);
                // Create new data format for correctAnswers
                Map<String, Object> correctAnswerItem = new HashMap<>();
                correctAnswerItem.put(answerItem.get("playerId").toString(), true);
                observer.getFirebaseGameReference().child("correctAnswers").updateChildren(correctAnswerItem);

                //TODO: Update score
            }
        }
    }

    @OnClick(R.id.createSubmitButton)
    protected void submitButton() {
        String answer = answerEditText.getText().toString();
        // Check if answer is valid
        if (!isValidAnswer(answer)) return;

        // Submit the answers to the game object
        submitAnswer(answer);

        // Clear field
        answerEditText.setText(null);
        answerTextInputLayout.setVisibility(View.INVISIBLE);
        createSubmitButton.setEnabled(false);
        createSubmitButton.setText("Waiting on Game Master to continue");
    }

    /**
     * Submits the answer of the player and sets it in the Firebase game object
     * @param answer    The answer to be submitted
     */
    private void submitAnswer(String answer) {
        // Create Map with player Id and their answer
        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put(observer.getFirebaseAuthenticationData().getUid(), answer);
        // Update the answers node in the game object
        observer.getFirebaseGameReference().child("answers").updateChildren(answerMap);
    }

    /**
     * Gets the current question set in the Firebase game object
     */
    private void getQuestion() {
        // Lookup the question ID
        observer.getFirebaseGameReference().child("question").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the current question ID in this game
                String questionId = (String) dataSnapshot.getValue().toString();
                // Lookup the actual question
                observer.getFirebaseQuestionsReference().child(questionId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot questionSnapshot) {
                        // Get the question and print it
                        Question question = questionSnapshot.getValue(Question.class);

                        if (getViewId() == PLAYER_VIEW) {
                            questionPlayerTextView.setText(question.getQuestion());
                        } else if (getViewId() == GAME_MASTER_VIEW) {
                            questionGameMasterTextView.setText(question.getQuestion());
                            answerTextView.setText(question.getAnswer());
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
    }

    /**
     * Helper method that checks if the answer is not empty
     * @param answer    The answer to be validated
     */
    private boolean isValidAnswer(String answer) {
        if (answer.isEmpty()) {
            answerTextInputLayout.setError(observer.getActivityReference().getString(R.string.error_empty_answer));
            createSubmitButton.setEnabled(false);
            return false;
        } else {
            answerTextInputLayout.setErrorEnabled(false);
            answerTextInputLayout.setError(null);
            return true;
        }
    }

}
