package com.arjanvlek.cyngnotainfo.views;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.arjanvlek.cyngnotainfo.Model.DeviceType;
import com.arjanvlek.cyngnotainfo.Model.UpdateLink;
import com.arjanvlek.cyngnotainfo.Model.UpdateType;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import java.util.ArrayList;
import java.util.List;

public class TutorialStep4Fragment extends Fragment {
    private View rootView;
    private SettingsManager settingsManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tutorial_4, container, false);
        settingsManager = new SettingsManager(getActivity().getApplicationContext());
        return rootView;
    }


    public void fetchUpdateMethods() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new UpdateDataFetcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, settingsManager.getPreference(SettingsManager.PROPERTY_DEVICE_TYPE));
        } else {
            new UpdateDataFetcher().execute(settingsManager.getPreference(SettingsManager.PROPERTY_DEVICE_TYPE));
        }
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
        Spinner spinner = (Spinner) rootView.findViewById(R.id.updateTypeSpinner);
        Integer position = 1;
        Resources resources = getResources();
        ArrayList<String> localizedUpdateTypes = new ArrayList<>();
        for (String updateType : updateTypes) {
            localizedUpdateTypes.add(getString(resources.getIdentifier(updateType, "string", getActivity().getPackageName())));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, localizedUpdateTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (localizedUpdateTypes.size() > 1) {
            spinner.setSelection(position);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String localizedUpdateTypeName = (String) adapterView.getItemAtPosition(i);
                String updateTypeName;
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
        }
    }
}
