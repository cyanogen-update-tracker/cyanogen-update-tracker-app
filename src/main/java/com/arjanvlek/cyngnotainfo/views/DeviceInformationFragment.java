package com.arjanvlek.cyngnotainfo.views;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.Model.DeviceInformationData;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.NetworkConnectionManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class DeviceInformationFragment extends Fragment {
    private RelativeLayout rootView;
    private AdView adView;
    private NetworkConnectionManager networkConnectionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //Inflate the layout for this fragment
        rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_deviceinformation, container, false);
        networkConnectionManager = new NetworkConnectionManager(getActivity().getApplicationContext());
        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        DeviceInformationData deviceInformationData = new DeviceInformationData();

        TextView deviceNameView = (TextView) rootView.findViewById(R.id.device_information_header);
        deviceNameView.setText(deviceInformationData.getDeviceManufacturer() + " " + deviceInformationData.getDeviceName());

        TextView socView = (TextView) rootView.findViewById(R.id.device_information_soc_field);
        socView.setText(deviceInformationData.getSOC());

        String cpuFreqString = deviceInformationData.getCPU_Frequency();
        TextView cpuFreqView = (TextView) rootView.findViewById(R.id.device_information_cpu_freq_field);
        if (!cpuFreqString.equals(DeviceInformationData.UNKNOWN)) {
            cpuFreqView.setText(deviceInformationData.getCPU_Frequency() + " " + getString(R.string.gigahertz));
        } else {
            cpuFreqView.setText(getString(R.string.unknown));
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
                memoryView.setText(totalMemory + " " + getString(R.string.megabyte));
            } else {
                View memoryLabel = rootView.findViewById(R.id.device_information_memory_label);
                memoryLabel.setVisibility(View.GONE);
                memoryView.setVisibility(View.GONE);
            }
        } else {
            memoryView.setText(getString(R.string.unknown));
        }


        TextView osVerView = (TextView) rootView.findViewById(R.id.device_information_os_ver_field);
        osVerView.setText(deviceInformationData.getOSVersion());

        TextView serialNumberView = (TextView) rootView.findViewById(R.id.device_information_serial_number_field);
        serialNumberView.setText(deviceInformationData.getSerialNumber());

        if (networkConnectionManager.checkNetworkConnection()) {
            showAds();
        } else {
            hideAds();
        }

    }

    private void hideAds() {
        if (adView != null) {
            adView.destroy();
        }
    }

    private void showAds() {

        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        adView = (AdView) rootView.findViewById(R.id.device_information_banner_field);

        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        String adsTestId = "7CFCF353FBC40363065F03DFAC7D7EE4";
        String adsTestId2 = "D9323E61DFC727F573528DB3820F7215";
        String adsTestId3 = "D732F1B481C5274B05D707AC197B33B2";
        String adsTestId4 = "3CFEF5EDED2F2CC6C866A48114EA2ECE";
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(adsTestId)
                .addTestDevice(adsTestId2)
                .addTestDevice(adsTestId3)
                .addTestDevice(adsTestId4)
                .addKeyword("smartphone")
                .addKeyword("tablet")
                .addKeyword("cyanogen")
                .addKeyword("android")
                .addKeyword("games")
                .build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }

    }

    /**
     * Called when the activity enters the foreground
     */
    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }

}
