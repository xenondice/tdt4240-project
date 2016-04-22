package com.tjuesyv.tjuesyv.states;

import android.support.design.widget.TextInputLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.firebaseObjects.Question;
import com.tjuesyv.tjuesyv.gameHandlers.GameObserver;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;

import java.util.HashMap;
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
    @Bind(R.id.questionTextView) TextView questionTextView;
    @Bind(R.id.answerEditText) EditText answerEditText;
    @Bind(R.id.answerTextInputLayout) TextInputLayout answerTextInputLayout;
    @Bind(R.id.answersGameMasterListView) ListView answersGameListView;

    private  static final int PLAYER_VIEW = 1;
    private  static final int GAME_MASTER_VIEW = 2;

    /**
     * Called once the state is entered
     *
     * @param observer
     */
    public CreateState(GameObserver observer) {
        super(observer);

        // Setup ButterKnife
        ButterKnife.bind(this, observer.getActivityReference());

        // Get the question for this round
        getQuestion();
    }

    @Override
    public int getViewId() {
        return PLAYER_VIEW;
    }

    @OnTextChanged(R.id.answerEditText)
    protected void validAnswer(CharSequence input) {
        isValidAnswer(input.toString());
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

        // Go to next state
        nextState();
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
                        questionTextView.setText(question.getQuestion());
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
            createSubmitButton.setEnabled(true);
            return true;
        }
    }

}
