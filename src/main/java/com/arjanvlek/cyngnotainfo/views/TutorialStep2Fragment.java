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

import com.arjanvlek.cyngnotainfo.MainActivity;
import com.arjanvlek.cyngnotainfo.Model.DeviceTypeEntity;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arjan on 9-6-2015. Part of Cyanogen Update Tracker.
 */
@SuppressWarnings("DefaultFileTemplate")
public class TutorialStep2Fragment extends Fragment {
    private View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tutorial_2,container,false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new DeviceDataFetcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            new DeviceDataFetcher().execute();
        }


    }


    private class DeviceDataFetcher extends AsyncTask<Void,Integer,List<DeviceTypeEntity>> {

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
        Spinner spinner = (Spinner)rootView.findViewById(R.id.deviceTypeSpinner);
        List<String>deviceNames = new ArrayList<>();

        for(DeviceTypeEntity deviceTypeEntity : deviceTypeEntities) {
            deviceNames.add(deviceTypeEntity.getDeviceType());
        }
        Integer position = null;
        String currentDeviceName = MainActivity.getPreference(MainActivity.PROPERTY_DEVICE_TYPE, getActivity().getApplicationContext());
        if(currentDeviceName != null) {
            for (int i = 0; i < deviceNames.size(); i++) {
                if (deviceNames.get(i).equals(currentDeviceName)) {
                    position = i;
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item, deviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if(position != null) {
            spinner.setSelection(position);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String deviceTypeName = (String) adapterView.getItemAtPosition(i);
                MainActivity.savePreference(MainActivity.PROPERTY_DEVICE_TYPE, deviceTypeName, getActivity().getApplicationContext());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });




    }
}
