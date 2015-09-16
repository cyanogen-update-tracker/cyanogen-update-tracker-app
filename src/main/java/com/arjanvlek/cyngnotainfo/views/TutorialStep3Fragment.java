package com.arjanvlek.cyngnotainfo.views;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.arjanvlek.cyngnotainfo.Model.Device;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;

import java.util.ArrayList;
import java.util.List;

public class TutorialStep3Fragment extends AbstractFragment {
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
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            new GetDevices().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            new GetDevices().execute();
        }
    }

    private class GetDevices extends AsyncTask<Void, Void, List<Device>> {

        @Override
        protected List<Device> doInBackground(Void... params) {
            return getApplicationContext().getServerConnector().getDevices();
        }

        @Override
        protected void onPostExecute(List<Device> devices) {
            fillDeviceSettings(devices);
        }
    }

    private void fillDeviceSettings(final List<Device> devices) {
        Spinner spinner = (Spinner) rootView.findViewById(R.id.settingsDeviceSpinner);
        List<String> deviceNames = new ArrayList<>();

        for (Device device : devices) {
            deviceNames.add(device.getDeviceName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, deviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String deviceName = (String) adapterView.getItemAtPosition(i);
                Long deviceId = 0L;
                for(Device device : devices) {
                    if(device.getDeviceName().equalsIgnoreCase(deviceName)) {
                        deviceId = device.getId();
                    }
                }
                settingsManager.savePreference(SettingsManager.PROPERTY_DEVICE, deviceName);
                settingsManager.saveLongPreference(SettingsManager.PROPERTY_DEVICE_ID, deviceId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });
    }
}
