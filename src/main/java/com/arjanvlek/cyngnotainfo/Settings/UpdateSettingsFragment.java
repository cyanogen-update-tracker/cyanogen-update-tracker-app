package com.arjanvlek.cyngnotainfo.Settings;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;

import com.arjanvlek.cyngnotainfo.MainActivity;
import com.arjanvlek.cyngnotainfo.R;

import java.util.ArrayList;

public class UpdateSettingsFragment extends DialogFragment {

    private SharedPreferences sharedPreferences;
    private int itemClicked;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList<String> updateTypes = getArguments().getStringArrayList("update_types");
        Resources resources = getResources();
        ArrayList<String> localizedUpdateTypes = new ArrayList<>();
        for(String updateType : updateTypes) {
            localizedUpdateTypes.add(getString(resources.getIdentifier(updateType, "string",getActivity().getPackageName())));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_single_choice, localizedUpdateTypes);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_update_type)
                .setSingleChoiceItems(adapter, 0,new DialogInterface.OnClickListener() {

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
                editor.putString(MainActivity.PROPERTY_UPDATE_TYPE, updateTypes.get(itemClicked));
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
