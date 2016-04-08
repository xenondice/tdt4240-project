package com.tjuesyv.tjuesyv;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by RayTM on 08.04.2016.
 */
public abstract class Prompter {

    AlertDialog.Builder builder;
    DialogInterface.OnClickListener listener;

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

    public void ask() {
        builder.show();
    }

    public abstract void callBack(boolean answer);
}
