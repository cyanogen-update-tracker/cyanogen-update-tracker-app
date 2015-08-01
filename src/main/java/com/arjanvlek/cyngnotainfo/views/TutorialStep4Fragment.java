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

import com.arjanvlek.cyngnotainfo.Model.UpdateDataLink;
import com.arjanvlek.cyngnotainfo.Model.UpdateMethod;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.*;

public class TutorialStep4Fragment extends AbstractFragment {
    private View rootView;
    private SettingsManager settingsManager;
    private long deviceId;
    private long updateMethodId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tutorial_4, container, false);
        settingsManager = new SettingsManager(getActivity().getApplicationContext());
        return rootView;
    }


    public void fetchUpdateMethods() {
        deviceId = settingsManager.getLongPreference(PROPERTY_DEVICE_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new UpdateDataFetcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, deviceId);
        } else {
            new UpdateDataFetcher().execute(deviceId);
        }
    }

    private class UpdateDataFetcher extends AsyncTask<Long, Integer, List<UpdateMethod>> {

        @Override
        public List<UpdateMethod> doInBackground(Long... deviceIds) {
            long deviceId = deviceIds[0];
            return getServerConnector().getUpdateMethods(deviceId);
        }

        @Override
        public void onPostExecute(List<UpdateMethod> updateMethods) {
            fillUpdateSettings(updateMethods);
        }
    }

    private void fillUpdateSettings(final List<UpdateMethod> updateMethods) {
        Spinner spinner = (Spinner) rootView.findViewById(R.id.settingsUpdateMethodSpinner);
        List<String> updateMethodNames = new ArrayList<>();
        if(Locale.getDefault().getDisplayLanguage().equals("Nederlands")) {
            for(UpdateMethod updateMethod : updateMethods) {
                updateMethodNames.add(updateMethod.getUpdateMethodNl());
            }
        }
        else {
            for(UpdateMethod updateMethod : updateMethods) {
                updateMethodNames.add(updateMethod.getUpdateMethod());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, updateMethodNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateMethodId = 0L;
                String updateMethodName = (String) adapterView.getItemAtPosition(i);
                for(UpdateMethod updateMethod : updateMethods) {
                    if(updateMethod.getUpdateMethod().equals(updateMethodName) || updateMethod.getUpdateMethodNl().equals(updateMethodName)) {
                        updateMethodId = updateMethod.getId();
                    }
                }

                //Set update type in preferences.
                settingsManager.saveLongPreference(PROPERTY_UPDATE_METHOD_ID, updateMethodId);
                settingsManager.savePreference(PROPERTY_UPDATE_METHOD, updateMethodName);
                //Set update link
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new UpdateDataLinkSetter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, deviceId, updateMethodId);
                } else {
                    new UpdateDataLinkSetter().execute(deviceId, updateMethodId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private class UpdateDataLinkSetter extends AsyncTask<Long, Integer, UpdateDataLink> {

        @Override
        public UpdateDataLink doInBackground(Long... params) {
            long deviceId = params[0];
            long updateMethodId = params[1];
            return  getServerConnector().getUpdateDataLink(deviceId, updateMethodId);
        }

        @Override
        public void onPostExecute(UpdateDataLink updateDataLink) {
            settingsManager.savePreference(PROPERTY_UPDATE_DATA_LINK, updateDataLink.getUpdateDataUrl());
        }
    }
}
