package com.arjanvlek.cyngnotainfo.Settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;

import com.arjanvlek.cyngnotainfo.R;

public class DeviceSettingsFragment extends DialogFragment {

private SharedPreferences sharedPreferences;
private int itemClicked;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_device_type)
                .setSingleChoiceItems(R.array.device_names_array, setCurrentlySelectedItem(), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemClicked = which;
                    }
                })
                .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sharedPreferences = getActivity().getPreferences(Context.MODE_APPEND);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        if (itemClicked == 0) {
                            editor.putString("device-name", "bacon");
                        } else if (itemClicked == 1) {
                            editor.putString("device-name", "tomato");
                        } else if (itemClicked == 2) {
                            editor.putString("device-name", "n1");
                        } else if (itemClicked == -1) {
                            editor.putString("device-name", "not-set");
                        }
                        editor.apply();

                    }
                })
        .setCancelable(false).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                onCreateDialog(null);
            }
        });
        return builder.create();
    }

    private int setCurrentlySelectedItem() {
        sharedPreferences = getActivity().getPreferences(Context.MODE_APPEND);
        if(sharedPreferences.getString("device-name", "not-set") != null) {
            switch (sharedPreferences.getString("device-name", "not-set")) {
                case "bacon":
                    return 0;
                case "tomato":
                    return 1;
                case "n1":
                    return 2;
                default:
                    return -1;

            }
        }
        return -1;
    }
}
