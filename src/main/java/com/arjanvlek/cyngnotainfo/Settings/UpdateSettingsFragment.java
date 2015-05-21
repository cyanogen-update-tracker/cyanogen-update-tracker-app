package com.arjanvlek.cyngnotainfo.Settings;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;

import com.arjanvlek.cyngnotainfo.R;

public class UpdateSettingsFragment extends DialogFragment {

private SharedPreferences sharedPreferences;
    private int itemClicked;
    private static String UPDATE_TYPE = "update-type";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_update_type)
                .setSingleChoiceItems(R.array.update_types_array,setCurrentlySelectedItem(),new DialogInterface.OnClickListener() {

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
                if(itemClicked == 0) {
                    editor.putString(UPDATE_TYPE, "stable");
                }
                else if (itemClicked == 1) {
                    editor.putString(UPDATE_TYPE, "incremental");
                }
                else if (itemClicked == -1) {
                    editor.putString(UPDATE_TYPE, "not-set");
                }
                editor.apply();
                Intent i = getActivity().getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getActivity().getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            }
        })
                .setCancelable(false)
        .setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                onCreateDialog(null);
            }
        });
        return builder.create();
    }

    private int setCurrentlySelectedItem() {
        sharedPreferences = getActivity().getPreferences(Context.MODE_APPEND);
        if(sharedPreferences.getString(UPDATE_TYPE, "not-set") != null) {
            switch (sharedPreferences.getString(UPDATE_TYPE, "not-set")) {
                case "stable":
                    return 0;
                case "incremental":
                    return 1;
                default:
                    return -1;

            }
        }
        return -1;
    }
}
