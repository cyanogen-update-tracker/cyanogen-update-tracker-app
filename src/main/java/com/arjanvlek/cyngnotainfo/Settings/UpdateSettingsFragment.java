package com.arjanvlek.cyngnotainfo.Settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;

import com.arjanvlek.cyngnotainfo.MainActivity;
import com.arjanvlek.cyngnotainfo.Model.DeviceTypeEntity;
import com.arjanvlek.cyngnotainfo.Model.UpdateLinkEntity;
import com.arjanvlek.cyngnotainfo.Model.UpdateTypeEntity;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UpdateSettingsFragment extends DialogFragment {

    private SharedPreferences sharedPreferences;
    private int itemClicked = 0;
    private ProgressDialog progressDialog;
    private Context context;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList<String> updateTypes = getArguments().getStringArrayList("update_types");
        Resources resources = getResources();
        ArrayList<String> localizedUpdateTypes = new ArrayList<>();
        for(String updateType : updateTypes) {
            localizedUpdateTypes.add(getString(resources.getIdentifier(updateType, "string",getActivity().getPackageName())));
        }
        context = getActivity();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_single_choice, localizedUpdateTypes);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_update_type)
                .setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemClicked = which;
                    }
                })
        .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sharedPreferences = getActivity().getPreferences(Context.MODE_APPEND);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(MainActivity.PROPERTY_UPDATE_TYPE, updateTypes.get(itemClicked));
                editor.putString(MainActivity.PROPERTY_UPDATE_LINK, "");
                editor.commit();

                new UpdateLinkSetter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sharedPreferences.getString(MainActivity.PROPERTY_DEVICE_TYPE, ""), sharedPreferences.getString(MainActivity.PROPERTY_UPDATE_TYPE, ""));


            }
        })
                .setCancelable(false);
        return builder.create();
    }

    private class UpdateLinkSetter extends AsyncTask<String,Integer,List<Object>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.saving_settings));
            progressDialog.setTitle(getString(R.string.loading));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();


        }

        @Override
        public List<Object> doInBackground(String... strings) {
            String deviceName = strings[0];
            String updateType = strings[1];
            ServerConnector serverConnector = new ServerConnector();
            List<Object> objects = new ArrayList<>();
            objects.add(serverConnector.getDeviceTypeEntities());
            objects.add(serverConnector.getUpdateTypeEntities());
            objects.add(serverConnector.getUpdateLinkEntities());
            objects.add(deviceName);
            objects.add(updateType);
            return objects;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onPostExecute(List<Object> entities) {
            ArrayList<DeviceTypeEntity> deviceTypeEntities = (ArrayList<DeviceTypeEntity>)entities.get(0);
            ArrayList<UpdateTypeEntity> updateTypeEntities = (ArrayList<UpdateTypeEntity>)entities.get(1);
            ArrayList<UpdateLinkEntity> updateLinkEntities = (ArrayList<UpdateLinkEntity>)entities.get(2);
            String deviceName = (String)entities.get(3);
            String updateType = (String)entities.get(4);
            Long deviceId = null;
            Long updateTypeId = null;
            String updateLink = null;
            for(DeviceTypeEntity deviceTypeEntity : deviceTypeEntities) {
                if(deviceTypeEntity.getDeviceType().equals(deviceName)) {
                    deviceId = deviceTypeEntity.getId();
                }
            }
            for(UpdateTypeEntity updateTypeEntity : updateTypeEntities) {
                if(updateTypeEntity.getUpdateType().equals(updateType)) {
                    updateTypeId = updateTypeEntity.getId();
                }
            }
            if(deviceId != null && updateTypeId != null) {
                for (UpdateLinkEntity updateLinkEntity : updateLinkEntities) {
                    if (updateLinkEntity.getTracking_device_type_id() == deviceId && updateLinkEntity.getTracking_update_type_id() == updateTypeId) {
                        updateLink = updateLinkEntity.getInformation_url();
                    }
                }
            }


            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(MainActivity.PROPERTY_UPDATE_LINK, updateLink);
            editor.commit();

            try {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
            catch(Exception ignored) {

            }
            Intent mStartActivity = new Intent(context, MainActivity.class);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            System.exit(0);



        }
    }

}
