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

public class NetworkErrorFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.app_requires_network));
        builder.setMessage(getString(R.string.app_requires_network_will_close));
        builder.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() { // Todo add xml string
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getActivity().finish();
                System.exit(0);

            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                getActivity().finish();
                System.exit(0);
            }
        })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
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
        return builder.create();
    }
}
