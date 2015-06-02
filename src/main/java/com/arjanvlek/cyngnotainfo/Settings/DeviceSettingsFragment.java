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
import com.arjanvlek.cyngnotainfo.Model.DeviceTypeEntity;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import java.util.ArrayList;
import java.util.List;

public class DeviceSettingsFragment extends DialogFragment {

    private SharedPreferences sharedPreferences;
    private int itemClicked;
    private List<DeviceTypeEntity>deviceTypeEntities = new ArrayList<>();
    private List<String>deviceNames = new ArrayList<>();
    private boolean loaded = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        new Thread(new fetchData()).start();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_single_choice, deviceNames);
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
                        if (itemClicked == 0) {
                            editor.putString(MainActivity.PROPERTY_DEVICE_TYPE, "OnePlus One");
                        } else if (itemClicked == 1) {
                            editor.putString(MainActivity.PROPERTY_DEVICE_TYPE, "Yu Yureka");
                        } else if (itemClicked == 2) {
                            editor.putString(MainActivity.PROPERTY_DEVICE_TYPE, "Oppo N1 CyanogenMod Edition");
                        } else if (itemClicked == -1) {
                            editor.putString(MainActivity.PROPERTY_DEVICE_TYPE, "");
                        }
                        editor.apply();

                    }
                })
        .setCancelable(false);
        return builder.create();
    }


    private class fetchData implements Runnable {
        @Override
        public void run() {

            ServerConnector serverConnector = new ServerConnector();
            List<DeviceTypeEntity> deviceTypeEntityList = serverConnector.getDeviceTypeEntities();
            for(DeviceTypeEntity deviceTypeEntity : deviceTypeEntityList) {
                deviceTypeEntities.add(deviceTypeEntity);
                deviceNames.add(deviceTypeEntity.getDeviceType());
            }
            loaded = true;
        }
    }
}
