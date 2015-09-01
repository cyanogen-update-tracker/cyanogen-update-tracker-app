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

public class ServerMaintenanceErrorFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.error_maintenance));
        builder.setMessage(getString(R.string.error_maintenance_message));
        builder.setPositiveButton(getString(R.string.error_exit_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getActivity().finish();
                System.exit(0);

            }
        }).setCancelable(false)
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

    @Override
    public void onDestroyView() {
        getActivity().finish();
        System.exit(0);
    }
}
