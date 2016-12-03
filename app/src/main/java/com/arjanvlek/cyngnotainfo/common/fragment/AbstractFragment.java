package com.arjanvlek.cyngnotainfo.common.fragment;

import android.support.v4.app.Fragment;

import com.arjanvlek.cyngnotainfo.common.internal.ApplicationData;



public abstract class AbstractFragment extends Fragment{

    private ApplicationData applicationData;
    //Test devices for ads.
    public static final String ADS_TEST_DEVICE_ID_OWN_DEVICE = "7CFCF353FBC40363065F03DFAC7D7EE4";
    public static final String ADS_TEST_DEVICE_ID_TEST_DEVICE = "F3C65C0A7317D335D140827A8200B825";
    public static final String ADS_TEST_DEVICE_ID_EMULATOR_1 = "D9323E61DFC727F573528DB3820F7215";
    public static final String ADS_TEST_DEVICE_ID_EMULATOR_2 = "D732F1B481C5274B05D707AC197B33B2";
    public static final String ADS_TEST_DEVICE_ID_EMULATOR_3 = "3CFEF5EDED2F2CC6C866A48114EA2ECE";

    public ApplicationData getApplicationData() {
        if(applicationData == null) {
            try {
                applicationData = (ApplicationData) getActivity().getApplication();
            } catch (Exception e) {
                applicationData = new ApplicationData();
            }
        }
        return applicationData;
    }
}
