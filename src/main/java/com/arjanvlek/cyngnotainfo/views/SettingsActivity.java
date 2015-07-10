package com.arjanvlek.cyngnotainfo.views;

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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.arjanvlek.cyngnotainfo.Model.DeviceType;
import com.arjanvlek.cyngnotainfo.Model.UpdateLink;
import com.arjanvlek.cyngnotainfo.Model.UpdateType;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.NetworkConnectionManager;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private SettingsManager settingsManager;
    private NetworkConnectionManager networkConnectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settingsManager = new SettingsManager(getApplicationContext());
        networkConnectionManager = new NetworkConnectionManager(getApplicationContext());
        progressBar = (ProgressBar) findViewById(R.id.settingsProgressBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                progressBar.setVisibility(View.VISIBLE);
            } catch (Exception ignored) {

            }
            new DeviceDataFetcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            try {
                progressBar.setVisibility(View.VISIBLE);
            } catch (Exception ignored) {

            }
            if (!networkConnectionManager.checkNetworkConnection()) {
                findViewById(R.id.settingsNoConnectionBar).setVisibility(View.VISIBLE);
                findViewById(R.id.settingsNoConnectionTextView).setVisibility(View.VISIBLE);
            }
            new DeviceDataFetcher().execute();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!networkConnectionManager.checkNetworkConnection()) {
            findViewById(R.id.settingsNoConnectionBar).setVisibility(View.VISIBLE);
            findViewById(R.id.settingsNoConnectionTextView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.settingsNoConnectionBar).setVisibility(View.GONE);
            findViewById(R.id.settingsNoConnectionTextView).setVisibility(View.GONE);
        }
    }

    private class DeviceDataFetcher extends AsyncTask<Void, Integer, List<DeviceType>> {

        @Override
        public List<DeviceType> doInBackground(Void... voids) {
            ServerConnector serverConnector = new ServerConnector();
            return serverConnector.getDeviceTypeEntities();
        }

        @Override
        public void onPostExecute(List<DeviceType> deviceTypeEntities) {
            fillDeviceSettings(deviceTypeEntities);

        }
    }

    private void fillDeviceSettings(List<DeviceType> deviceTypeEntities) {
        Spinner spinner = (Spinner) findViewById(R.id.deviceTypeSpinner);
        List<String> deviceNames = new ArrayList<>();

        for (DeviceType deviceType : deviceTypeEntities) {
            deviceNames.add(deviceType.getDeviceType());
        }
        Integer position = null;
        String currentDeviceName = settingsManager.getPreference(SettingsManager.PROPERTY_DEVICE_TYPE);
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
                settingsManager.savePreference(SettingsManager.PROPERTY_DEVICE_TYPE, deviceTypeName);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    try {
                        progressBar.setVisibility(View.VISIBLE);
                    } catch (Exception ignored) {

                    }
                    new UpdateDataFetcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, deviceTypeName);
                } else {
                    try {
                        progressBar.setVisibility(View.VISIBLE);
                    } catch (Exception ignored) {

                    }
                    new UpdateDataFetcher().execute(deviceTypeName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });


    }

    private class UpdateDataFetcher extends AsyncTask<String, Integer, List<UpdateType>> {

        @Override
        public List<UpdateType> doInBackground(String... strings) {
            String deviceName = strings[0];
            String deviceId = null;
            ServerConnector serverConnector = new ServerConnector();
            List<DeviceType> deviceTypeEntities = serverConnector.getDeviceTypeEntities();
            for (DeviceType deviceType : deviceTypeEntities) {
                if (deviceType.getDeviceType().equals(deviceName)) {
                    deviceId = String.valueOf(deviceType.getId());
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
        public void onPostExecute(List<UpdateType> updateTypeEntities) {
            ArrayList<String> updateTypeNames = new ArrayList<>();

            for (UpdateType updateType : updateTypeEntities) {
                updateTypeNames.add(updateType.getUpdateType());
            }
            fillUpdateSettings(updateTypeNames);

        }
    }

    private void fillUpdateSettings(ArrayList<String> updateTypes) {
        Spinner spinner = (Spinner) findViewById(R.id.updateTypeSpinner);
        String currentUpdateType = settingsManager.getPreference(SettingsManager.PROPERTY_UPDATE_METHOD);
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
                try {
                    progressBar.setVisibility(View.VISIBLE);
                } catch (Exception ignored) {

                }
                if (localizedUpdateTypeName.equals(getString(R.string.full_update))) {
                    updateTypeName = SettingsManager.FULL_UPDATE;
                } else {
                    updateTypeName = SettingsManager.INCREMENTAL_UPDATE;
                }
                //Set update type in preferences.
                settingsManager.savePreference(SettingsManager.PROPERTY_UPDATE_METHOD, updateTypeName);
                //Set update link
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new UpdateLinkSetter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, settingsManager.getPreference(SettingsManager.PROPERTY_DEVICE_TYPE), updateTypeName);
                } else {
                    new UpdateLinkSetter().execute(settingsManager.getPreference(SettingsManager.PROPERTY_DEVICE_TYPE), updateTypeName);
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
            List<DeviceType> deviceTypeEntities = serverConnector.getDeviceTypeEntities();
            for (DeviceType deviceType : deviceTypeEntities) {
                if (deviceType.getDeviceType().equals(deviceName)) {
                    deviceId = String.valueOf(deviceType.getId());
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
            ArrayList<DeviceType> deviceTypeEntities = (ArrayList<DeviceType>) entities.get(0);
            ArrayList<UpdateType> updateTypeEntities = (ArrayList<UpdateType>) entities.get(1);
            ArrayList<UpdateLink> updateLinkEntities = (ArrayList<UpdateLink>) entities.get(2);
            String deviceName = (String) entities.get(3);
            String updateType = (String) entities.get(4);
            Long deviceId = null;
            Long updateTypeId = null;
            String updateLink = null;
            for (DeviceType deviceType : deviceTypeEntities) {
                if (deviceType.getDeviceType().equals(deviceName)) {
                    deviceId = deviceType.getId();
                }
            }
            for (UpdateType updateTypeEntity : updateTypeEntities) {
                if (updateTypeEntity.getUpdateType().equals(updateType)) {
                    updateTypeId = updateTypeEntity.getId();
                }
            }
            if (deviceId != null && updateTypeId != null) {
                for (UpdateLink updateLinkEntity : updateLinkEntities) {
                    if (updateLinkEntity.getTracking_device_type_id() == deviceId && updateLinkEntity.getTracking_update_type_id() == updateTypeId) {
                        updateLink = updateLinkEntity.getInformation_url();
                    }
                }
            }
            settingsManager.savePreference(SettingsManager.PROPERTY_UPDATE_LINK, updateLink);
            try {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            } catch (Exception ignored) {

            }
        }
    }

    private void showSettingsWarning() {
        Toast.makeText(this, getString(R.string.settings_entered_incorrectly), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onBackPressed() {
        if (settingsManager.checkIfSettingsAreValid() && !progressBar.isShown()) {
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
                if (settingsManager.checkIfSettingsAreValid() && !progressBar.isShown()) {
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
