package com.arjanvlek.cyngnotainfo.views;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.Model.DeviceInformationData;
import com.arjanvlek.cyngnotainfo.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class DeviceInformationFragment extends Fragment {
    private RelativeLayout rootView;
    private AdView mAdView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //Inflate the layout for this fragment
        rootView = (RelativeLayout)inflater.inflate(R.layout.fragment_deviceinformation, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        DeviceInformationData deviceInformationData = new DeviceInformationData();

        TextView deviceNameView = (TextView)rootView.findViewById(R.id.device_information_header);
        deviceNameView.setText(deviceInformationData.getDeviceManufacturer() + " " + deviceInformationData.getDeviceName());

        TextView socView = (TextView)rootView.findViewById(R.id.device_information_soc_field);
        socView.setText(deviceInformationData.getSOC());

        String cpuFreqString = deviceInformationData.getCPU_Frequency();
        TextView cpuFreqView = (TextView)rootView.findViewById(R.id.device_information_cpu_freq_field);
        if(!cpuFreqString.equals(DeviceInformationData.UNKNOWN)) {
            cpuFreqView.setText(deviceInformationData.getCPU_Frequency() + " " + getString(R.string.gigahertz));
        }
        else {
            cpuFreqView.setText(getString(R.string.unknown));
        }


        String memoryString = deviceInformationData.getMemoryAmount();
        TextView memoryView = (TextView) rootView.findViewById(R.id.device_information_memory_field);
        if(!memoryString.equals(DeviceInformationData.UNKNOWN)) {

            memoryView.setText(memoryString + " " + getString(R.string.gigabyte));
        }
        else {
            memoryView.setText(getString(R.string.unknown));
        }


        TextView osVerView = (TextView)rootView.findViewById(R.id.device_information_os_ver_field);
        osVerView.setText(deviceInformationData.getOSVersion());

        TextView serialNumberView = (TextView)rootView.findViewById(R.id.device_information_serial_number_field);
        serialNumberView.setText(deviceInformationData.getSerialNumber());

        if(checkNetworkConnection()) {
            showAds();
        }
        else {
            hideAds();
        }

    }

    private void hideAds() {
        if(mAdView != null ) {
            mAdView.destroy();
        }
    }

    private void showAds() {

        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        mAdView = (AdView) rootView.findViewById(R.id.device_information_banner_field);

        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        String adsTestId = "7CFCF353FBC40363065F03DFAC7D7EE4";
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(adsTestId)
                .addKeyword("smartphone")
                .addKeyword("tablet")
                .addKeyword("cyanogen")
                .addKeyword("android")
                .addKeyword("games")
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
    }
    private boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

}
