package com.arjanvlek.cyngnotainfo.views;

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
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import java.util.ArrayList;
import java.util.List;

public class TutorialStep3Fragment extends Fragment {
    private View rootView;
    private SettingsManager settingsManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tutorial_3, container, false);
        settingsManager = new SettingsManager(getActivity().getApplicationContext());
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new DeviceDataFetcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new DeviceDataFetcher().execute();
        }
    }


    private class DeviceDataFetcher extends AsyncTask<Void, Integer, List<DeviceType>> {

        @Override
        public List<DeviceType> doInBackground(Void... voids) {
            ServerConnector serverConnector = new ServerConnector();
            return serverConnector.getDeviceTypeEntities();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onPostExecute(List<DeviceType> deviceTypeEntities) {
            fillDeviceSettings(deviceTypeEntities);

        }
    }

    private void fillDeviceSettings(List<DeviceType> deviceTypeEntities) {
        Spinner spinner = (Spinner) rootView.findViewById(R.id.deviceTypeSpinner);
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, deviceNames);
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });
    }
}
