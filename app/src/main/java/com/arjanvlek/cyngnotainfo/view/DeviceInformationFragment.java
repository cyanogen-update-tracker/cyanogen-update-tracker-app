package com.arjanvlek.cyngnotainfo.view;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.Model.Device;
import com.arjanvlek.cyngnotainfo.Model.DeviceInformationData;
import com.arjanvlek.cyngnotainfo.Model.SystemVersionProperties;
import com.arjanvlek.cyngnotainfo.R;

import java.util.List;

import static com.arjanvlek.cyngnotainfo.ApplicationContext.NO_CYANOGEN_OS;

public class DeviceInformationFragment extends AbstractFragment {
    private RelativeLayout rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //Inflate the layout for this fragment
        rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_deviceinformation, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(isAdded()) {
            displayDeviceInformation(null); // To fast load device information with generic / non-pretty device name.
            new GetDevices().execute();
        }
    }

    private void displayDeviceInformation(@Nullable List<Device> devices) {
        if(isAdded()) {
            DeviceInformationData deviceInformationData = new DeviceInformationData();

            String deviceName = null;
            SystemVersionProperties systemVersionProperties = getApplicationContext().getSystemVersionProperties();

            if (devices != null) {
                for (Device device : devices) {
                    if (device.getModelNumber() != null && device.getModelNumber().equals(systemVersionProperties.getCyanogenDeviceCodeName())) {
                        deviceName = device.getDeviceName();
                    }
                }
            }


            TextView deviceNameView = (TextView) rootView.findViewById(R.id.device_information_header);
            if (devices == null || deviceName == null) {
                deviceNameView.setText(String.format(getString(R.string.device_information_device_name), deviceInformationData.getDeviceManufacturer(), deviceInformationData.getDeviceName()));
            } else {
                deviceNameView.setText(deviceName);
            }

            TextView socView = (TextView) rootView.findViewById(R.id.device_information_soc_field);
            socView.setText(deviceInformationData.getSoc());

            String cpuFreqString = deviceInformationData.getCpuFrequency();
            TextView cpuFreqView = (TextView) rootView.findViewById(R.id.device_information_cpu_freq_field);
            if (!cpuFreqString.equals(DeviceInformationData.UNKNOWN)) {
                cpuFreqView.setText(String.format(getString(R.string.device_information_gigahertz), deviceInformationData.getCpuFrequency()));
            } else {
                cpuFreqView.setText(getString(R.string.device_information_unknown));
            }

            long totalMemory = 0;
            try {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                ActivityManager activityManager = (ActivityManager) getActivity().getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
                activityManager.getMemoryInfo(mi);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    totalMemory = mi.totalMem / 1048576L;
                } else {
                    totalMemory = 1;
                }
            } catch (Exception ignored) {

            }
            TextView memoryView = (TextView) rootView.findViewById(R.id.device_information_memory_field);
            if (totalMemory != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    memoryView.setText(String.format(getString(R.string.download_size_megabyte), totalMemory));
                } else {
                    View memoryLabel = rootView.findViewById(R.id.device_information_memory_label);
                    memoryLabel.setVisibility(View.GONE);
                    memoryView.setVisibility(View.GONE);
                }
            } else {
                memoryView.setText(getString(R.string.device_information_unknown));
            }

            TextView cyanogenOsVerView = (TextView) rootView.findViewById(R.id.device_information_cyanogen_os_ver_field);

            if (!systemVersionProperties.getCyanogenOSVersion().equals(NO_CYANOGEN_OS)) {
                cyanogenOsVerView.setText(systemVersionProperties.getCyanogenOSVersion());

            } else {
                TextView cyanogenOsVerLabel = (TextView) rootView.findViewById(R.id.device_information_cyanogen_os_ver_label);
                cyanogenOsVerLabel.setVisibility(View.GONE);
                cyanogenOsVerView.setVisibility(View.GONE);
            }

            TextView osVerView = (TextView) rootView.findViewById(R.id.device_information_os_ver_field);
            osVerView.setText(deviceInformationData.getOsVersion());

            TextView osIncrementalView = (TextView) rootView.findViewById(R.id.device_information_incremental_os_ver_field);
            osIncrementalView.setText(deviceInformationData.getIncrementalOsVersion());

            TextView osPatchDateView = (TextView) rootView.findViewById(R.id.device_information_os_patch_level_field);

            if (!systemVersionProperties.getSecurityPatchDate().equals(NO_CYANOGEN_OS)) {
                osPatchDateView.setText(systemVersionProperties.getSecurityPatchDate());
            } else {
                TextView osPatchDateLabel = (TextView) rootView.findViewById(R.id.device_information_os_patch_level_label);
                osPatchDateLabel.setVisibility(View.GONE);
                osPatchDateView.setVisibility(View.GONE);
            }

            TextView serialNumberView = (TextView) rootView.findViewById(R.id.device_information_serial_number_field);
            serialNumberView.setText(deviceInformationData.getSerialNumber());
        }
    }

    private class GetDevices extends AsyncTask<Void, Void, List<Device>> {

        @Override
        protected List<Device> doInBackground(Void... params) {
            try {
                return getApplicationContext().getDevices();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Device> devices) {
            displayDeviceInformation(devices);
        }
    }
}
