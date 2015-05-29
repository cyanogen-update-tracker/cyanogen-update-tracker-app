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

import com.arjanvlek.cyngnotainfo.MainActivity;
import com.arjanvlek.cyngnotainfo.R;

public class UpdateSettingsFragment extends DialogFragment {

private SharedPreferences sharedPreferences;
    private int itemClicked;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_update_type)
                .setSingleChoiceItems(R.array.update_types_array, 0,new DialogInterface.OnClickListener() {

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
                    editor.putString(MainActivity.PROPERTY_UPDATE_TYPE, MainActivity.FULL_UPDATE);
                }
                else if (itemClicked == 1) {
                    editor.putString(MainActivity.PROPERTY_UPDATE_TYPE, MainActivity.INCREMENTAL_UPDATE);
                }
                else if (itemClicked == -1) {
                    editor.putString(MainActivity.PROPERTY_UPDATE_TYPE, "");
                }
                editor.apply();
                Intent i = getActivity().getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getActivity().getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            }
        })
                .setCancelable(false);
        return builder.create();
    }

}
