package com.arjanvlek.cyngnotainfo.Settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;

import com.arjanvlek.cyngnotainfo.MainActivity;
import com.arjanvlek.cyngnotainfo.Model.DeviceTypeEntity;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import java.util.List;

public class DeviceSettingsFragment extends DialogFragment {

private SharedPreferences sharedPreferences;
private int itemClicked;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_device_type)
                .setSingleChoiceItems(R.array.device_names_array, 0, new DialogInterface.OnClickListener() {

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

    private class serverInfoFetch implements Runnable {

        @Override
        public void run() {
            ServerConnector serverConnector = new ServerConnector();
            try {
                List<DeviceTypeEntity> deviceTypeEntityList = serverConnector.getDeviceTypeEntities();
                for(DeviceTypeEntity deviceTypeEntity : deviceTypeEntityList) {
                    System.out.println(deviceTypeEntity.getDeviceType());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
