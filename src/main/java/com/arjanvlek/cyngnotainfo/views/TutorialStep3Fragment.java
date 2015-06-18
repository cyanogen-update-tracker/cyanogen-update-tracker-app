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

import com.arjanvlek.cyngnotainfo.MainActivity;
import com.arjanvlek.cyngnotainfo.Model.DeviceTypeEntity;
import com.arjanvlek.cyngnotainfo.Model.UpdateLinkEntity;
import com.arjanvlek.cyngnotainfo.Model.UpdateTypeEntity;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arjan on 9-6-2015. Part of Cyanogen Update Tracker.
 */
@SuppressWarnings("DefaultFileTemplate")
public class TutorialStep3Fragment extends Fragment {
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tutorial_3, container, false);
        return rootView;
    }


    public void fetchUpdateMethods() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new UpdateDataFetcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MainActivity.getPreference(MainActivity.PROPERTY_DEVICE_TYPE, getActivity().getApplicationContext()));
        } else {
            new UpdateDataFetcher().execute(MainActivity.getPreference(MainActivity.PROPERTY_DEVICE_TYPE, getActivity().getApplicationContext()));
        }
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
        Spinner spinner = (Spinner) rootView.findViewById(R.id.updateTypeSpinner);
        String currentUpdateType = MainActivity.getPreference(MainActivity.PROPERTY_UPDATE_TYPE, getActivity().getApplicationContext());
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
                    updateTypeName = MainActivity.FULL_UPDATE;
                } else {
                    updateTypeName = MainActivity.INCREMENTAL_UPDATE;
                }
                //Set update type in preferences.
                MainActivity.savePreference(MainActivity.PROPERTY_UPDATE_TYPE, updateTypeName, getActivity().getApplicationContext());
                //Set update link
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new UpdateLinkSetter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MainActivity.getPreference(MainActivity.PROPERTY_DEVICE_TYPE, getActivity().getApplicationContext()), updateTypeName);
                } else {
                    new UpdateLinkSetter().execute(MainActivity.getPreference(MainActivity.PROPERTY_DEVICE_TYPE, getActivity().getApplicationContext()), updateTypeName);
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
            MainActivity.savePreference(MainActivity.PROPERTY_UPDATE_LINK, updateLink, getActivity().getApplicationContext());
        }
    }
}
