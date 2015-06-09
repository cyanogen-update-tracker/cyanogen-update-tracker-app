package com.arjanvlek.cyngnotainfo.views;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.arjanvlek.cyngnotainfo.MainActivity;
import com.arjanvlek.cyngnotainfo.Model.DeviceTypeEntity;
import com.arjanvlek.cyngnotainfo.Model.UpdateLinkEntity;
import com.arjanvlek.cyngnotainfo.Model.UpdateTypeEntity;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new DeviceDataFetcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new DeviceDataFetcher().execute();
        }
    }


    private class DeviceDataFetcher extends AsyncTask<Void, Integer, List<DeviceTypeEntity>> {

        @Override
        public List<DeviceTypeEntity> doInBackground(Void... voids) {
            ServerConnector serverConnector = new ServerConnector();
            return serverConnector.getDeviceTypeEntities();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onPostExecute(List<DeviceTypeEntity> deviceTypeEntities) {
            fillDeviceSettings(deviceTypeEntities);

        }
    }

    private void fillDeviceSettings(List<DeviceTypeEntity> deviceTypeEntities) {
        Spinner spinner = (Spinner) findViewById(R.id.deviceTypeSpinner);
        List<String> deviceNames = new ArrayList<>();

        for (DeviceTypeEntity deviceTypeEntity : deviceTypeEntities) {
            deviceNames.add(deviceTypeEntity.getDeviceType());
        }
        Integer position = null;
        String currentDeviceName = MainActivity.getPreference(MainActivity.PROPERTY_DEVICE_TYPE, getApplicationContext());
        if (currentDeviceName != null) {
            for (int i = 0; i < deviceNames.size(); i++) {
                if (deviceNames.get(i).equals(currentDeviceName)) {
                    position = i;
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (position != null) {
            spinner.setSelection(position);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String deviceTypeName = (String) adapterView.getItemAtPosition(i);
                MainActivity.savePreference(MainActivity.PROPERTY_DEVICE_TYPE, deviceTypeName, getApplicationContext());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new UpdateDataFetcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, deviceTypeName);
                } else {
                    new UpdateDataFetcher().execute(deviceTypeName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });


    }

    private class UpdateDataFetcher extends AsyncTask<String, Integer, List<UpdateTypeEntity>> {

        @Override
        public List<UpdateTypeEntity> doInBackground(String... strings) {
            String deviceName = strings[0];
            String deviceId = null;
            ServerConnector serverConnector = new ServerConnector();
            List<DeviceTypeEntity> deviceTypeEntities = serverConnector.getDeviceTypeEntities();
            for (DeviceTypeEntity deviceTypeEntity : deviceTypeEntities) {
                if (deviceTypeEntity.getDeviceType().equals(deviceName)) {
                    deviceId = String.valueOf(deviceTypeEntity.getId());
                }
            }
            if (deviceId != null) {
                if (deviceId.equals("null")) {
                    deviceId = null;
                }
            }
            return serverConnector.getUpdateTypeEntities(deviceId);
        }

        @Override
        public void onPostExecute(List<UpdateTypeEntity> updateTypeEntities) {
            ArrayList<String> updateTypeNames = new ArrayList<>();

            for (UpdateTypeEntity updateTypeEntity : updateTypeEntities) {
                updateTypeNames.add(updateTypeEntity.getUpdateType());
            }
            fillUpdateSettings(updateTypeNames);

        }
    }

    private void fillUpdateSettings(ArrayList<String> updateTypes) {
        Spinner spinner = (Spinner) findViewById(R.id.updateTypeSpinner);
        String currentUpdateType = MainActivity.getPreference(MainActivity.PROPERTY_UPDATE_TYPE, getApplicationContext());
        Integer position = null;
        if (currentUpdateType != null) {
            for (int i = 0; i < updateTypes.size(); i++) {
                if (updateTypes.get(i).equals(currentUpdateType)) {
                    position = i;
                }
            }
        }
        Resources resources = getResources();
        ArrayList<String> localizedUpdateTypes = new ArrayList<>();
        for (String updateType : updateTypes) {
            localizedUpdateTypes.add(getString(resources.getIdentifier(updateType, "string", getPackageName())));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, localizedUpdateTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (position != null) {
            spinner.setSelection(position);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String localizedUpdateTypeName = (String) adapterView.getItemAtPosition(i);
                String updateTypeName;
                if (localizedUpdateTypeName.equals(getString(R.string.full_update))) {
                    updateTypeName = MainActivity.FULL_UPDATE;
                } else {
                    updateTypeName = MainActivity.INCREMENTAL_UPDATE;
                }
                //Set update type in preferences.
                MainActivity.savePreference(MainActivity.PROPERTY_UPDATE_TYPE, updateTypeName, getApplicationContext());
                //Set update link
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new UpdateLinkSetter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MainActivity.getPreference(MainActivity.PROPERTY_DEVICE_TYPE, getApplicationContext()), updateTypeName);
                } else {
                    new UpdateLinkSetter().execute(MainActivity.getPreference(MainActivity.PROPERTY_DEVICE_TYPE, getApplicationContext()), updateTypeName);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private class UpdateLinkSetter extends AsyncTask<String, Integer, List<Object>> {
        @Override
        public List<Object> doInBackground(String... strings) {
            String deviceName = strings[0];
            String updateType = strings[1];
            String deviceId = null;
            ServerConnector serverConnector = new ServerConnector();
            List<DeviceTypeEntity> deviceTypeEntities = serverConnector.getDeviceTypeEntities();
            for (DeviceTypeEntity deviceTypeEntity : deviceTypeEntities) {
                if (deviceTypeEntity.getDeviceType().equals(deviceName)) {
                    deviceId = String.valueOf(deviceTypeEntity.getId());
                }
            }
            if (deviceId != null) {
                if (deviceId.equals("null")) {
                    deviceId = null;
                }
            }
            List<Object> objects = new ArrayList<>();
            objects.add(serverConnector.getDeviceTypeEntities());
            objects.add(serverConnector.getUpdateTypeEntities(deviceId));
            objects.add(serverConnector.getUpdateLinkEntities());
            objects.add(deviceName);
            objects.add(updateType);
            return objects;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onPostExecute(List<Object> entities) {
            ArrayList<DeviceTypeEntity> deviceTypeEntities = (ArrayList<DeviceTypeEntity>) entities.get(0);
            ArrayList<UpdateTypeEntity> updateTypeEntities = (ArrayList<UpdateTypeEntity>) entities.get(1);
            ArrayList<UpdateLinkEntity> updateLinkEntities = (ArrayList<UpdateLinkEntity>) entities.get(2);
            String deviceName = (String) entities.get(3);
            String updateType = (String) entities.get(4);
            Long deviceId = null;
            Long updateTypeId = null;
            String updateLink = null;
            for (DeviceTypeEntity deviceTypeEntity : deviceTypeEntities) {
                if (deviceTypeEntity.getDeviceType().equals(deviceName)) {
                    deviceId = deviceTypeEntity.getId();
                }
            }
            for (UpdateTypeEntity updateTypeEntity : updateTypeEntities) {
                if (updateTypeEntity.getUpdateType().equals(updateType)) {
                    updateTypeId = updateTypeEntity.getId();
                }
            }
            if (deviceId != null && updateTypeId != null) {
                for (UpdateLinkEntity updateLinkEntity : updateLinkEntities) {
                    if (updateLinkEntity.getTracking_device_type_id() == deviceId && updateLinkEntity.getTracking_update_type_id() == updateTypeId) {
                        updateLink = updateLinkEntity.getInformation_url();
                    }
                }
            }
            MainActivity.savePreference(MainActivity.PROPERTY_UPDATE_LINK, updateLink, getApplicationContext());
        }
    }

    private boolean checkIfSettingsAreValid() {
        return MainActivity.checkPreference(MainActivity.PROPERTY_DEVICE_TYPE, getApplicationContext()) && MainActivity.checkPreference(MainActivity.PROPERTY_UPDATE_TYPE, getApplicationContext()) && MainActivity.checkPreference(MainActivity.PROPERTY_UPDATE_LINK, getApplicationContext());
    }

    private void showSettingsWarning() {
        Toast.makeText(this, getString(R.string.settings_entered_incorrectly), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onBackPressed() {
        if (checkIfSettingsAreValid()) {
            NavUtils.navigateUpFromSameTask(this);
        } else {
            showSettingsWarning();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (checkIfSettingsAreValid()) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                } else {
                    showSettingsWarning();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }
}
