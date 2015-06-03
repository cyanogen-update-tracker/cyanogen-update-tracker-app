package com.arjanvlek.cyngnotainfo.Settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.widget.ArrayAdapter;

import com.arjanvlek.cyngnotainfo.MainActivity;
import com.arjanvlek.cyngnotainfo.R;

import java.util.ArrayList;

public class DeviceSettingsFragment extends DialogFragment {

    private SharedPreferences sharedPreferences;
    private int itemClicked;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList<String> devices = getArguments().getStringArrayList("devices");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_single_choice, devices);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_device_type)
                .setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {

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
                        editor.putString(MainActivity.PROPERTY_DEVICE_TYPE, devices.get(itemClicked));
                        editor.apply();

                    }
                })
        .setCancelable(false);
        return builder.create();
    }



}
