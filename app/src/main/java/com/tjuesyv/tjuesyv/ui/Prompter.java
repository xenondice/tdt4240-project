package com.tjuesyv.tjuesyv.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.tjuesyv.tjuesyv.R;

/**
 * Created by RayTM on 08.04.2016.
 */
public abstract class Prompter {

    AlertDialog.Builder builder;
    DialogInterface.OnClickListener listener;

    /**
     * Create a yes or no prompt
     * Remeber to implement the callback function in the form of a anonymous function
     * @param prompt
     * @param context
     */
    public Prompter(CharSequence prompt, Context context) {
        listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        callBack(true);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        callBack(false);
                        break;
                }
            }
        };

        builder = new AlertDialog.Builder(context);
        builder.setMessage(prompt)
                .setPositiveButton(context.getText(R.string.prompt_positive), listener)
                .setNegativeButton(context.getText(R.string.prompt_negative), listener);
    }

    /**
     * Show the prompt to the user now
     */
    public void ask() {
        builder.show();
    }

    /**
     * This function is called once the user has answered something
     * The asnwer boolean is true if yes was pressed, and false otherwise
     * @param answer
     */
    public abstract void callBack(boolean answer);
}
