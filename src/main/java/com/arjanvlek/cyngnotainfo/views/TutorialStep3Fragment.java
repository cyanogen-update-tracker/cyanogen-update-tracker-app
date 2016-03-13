package com.arjanvlek.cyngnotainfo.views;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.arjanvlek.cyngnotainfo.Model.Device;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.CustomDropdown;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;

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
        new GetDevices().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

        int selectedIndex = -1;

        for(int i=0; i<devices.size(); i++) {
            if(devices.get(i).getModelNumber() != null && devices.get(i).getModelNumber().equals(getSystemVersionProperties().getModelNumber())) {
                selectedIndex = i;
            }
        }

        final int selection = selectedIndex;

        ArrayAdapter<Device> adapter = new ArrayAdapter<Device>(getActivity(), android.R.layout.simple_spinner_item, devices) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return CustomDropdown.initCustomDeviceDropdown(position, convertView, parent, android.R.layout.simple_spinner_item, devices, selection, this.getContext());
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                return CustomDropdown.initCustomDeviceDropdown(position, convertView, parent, android.R.layout.simple_spinner_dropdown_item, devices, selection, this.getContext());
            }


    };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if(selectedIndex != -1) {
            spinner.setSelection(selectedIndex);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Device device = (Device)adapterView.getItemAtPosition(i);
                settingsManager.savePreference(SettingsManager.PROPERTY_DEVICE, device.getDeviceName());
                settingsManager.saveLongPreference(SettingsManager.PROPERTY_DEVICE_ID, device.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });
    }
}
