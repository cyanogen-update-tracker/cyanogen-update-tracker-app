package com.arjanvlek.cyngnotainfo.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;

import com.arjanvlek.cyngnotainfo.R;

/**
 * Usage: Title text, Message text, Positive button text, Negative button text.
 */
public class MessageDialog extends DialogFragment {
    private boolean isClosable;
    private ErrorDialogListener errorDialogListener;

    public interface ErrorDialogListener {
        void onDialogRetryButtonClick(DialogFragment dialogFragment);
        void onDialogCancelButtonClick(DialogFragment dialogFragment);
        void onDialogGooglePlayButtonClick(DialogFragment dialogFragment);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            errorDialogListener = (ErrorDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            errorDialogListener = null;
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle arguments = getArguments();
        builder.setTitle(arguments.getString("title"));
        builder.setMessage(arguments.getString("message"));
        String button1Text = arguments.getString("button1");
        String button2Text = arguments.getString("button2");
        isClosable = arguments.getBoolean("closable");

        if(button2Text == null && button1Text != null) {
            builder.setPositiveButton(button1Text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    errorDialogListener.onDialogCancelButtonClick(MessageDialog.this);
                }
            });
        }
        if(button2Text != null && button1Text != null && button1Text.toUpperCase().equals(getString(R.string.error_button_retry).toUpperCase())) {
            builder.setPositiveButton(button1Text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    errorDialogListener.onDialogRetryButtonClick(MessageDialog.this);
                }
            });
            builder.setNegativeButton(button2Text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    errorDialogListener.onDialogCancelButtonClick(MessageDialog.this);
                }
            });
        }
        if(button2Text != null && button1Text != null && button1Text.equals(getString(R.string.error_google_play_button_text))) {
            builder.setPositiveButton(button1Text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    errorDialogListener.onDialogGooglePlayButtonClick(MessageDialog.this);
                }
            });
            builder.setNegativeButton(button2Text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    errorDialogListener.onDialogCancelButtonClick(MessageDialog.this);
                }
            });
        }
        if(!isClosable) {
            builder.setCancelable(false);
            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                    if (i == KeyEvent.KEYCODE_BACK) {
                        getActivity().finish();
                        System.exit(0);
                    }
                    return true;
                }
            });
            if (Build.VERSION.SDK_INT >= 17) {
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        getActivity().finish();
                        System.exit(0);
                    }
                });
            }
        }
        return builder.create();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(!isClosable) {
            getActivity().finish();
            System.exit(0);
        }
    }
}
