package com.arjanvlek.cyngnotainfo.view;

import android.support.v4.app.Fragment;

import com.arjanvlek.cyngnotainfo.ApplicationContext;



public abstract class AbstractFragment extends Fragment{

    private ApplicationContext applicationContext;
    //Test devices for ads.
    public static final String ADS_TEST_DEVICE_ID_OWN_DEVICE = "7CFCF353FBC40363065F03DFAC7D7EE4";
    public static final String ADS_TEST_DEVICE_ID_EMULATOR_1 = "D9323E61DFC727F573528DB3820F7215";
    public static final String ADS_TEST_DEVICE_ID_EMULATOR_2 = "D732F1B481C5274B05D707AC197B33B2";
    public static final String ADS_TEST_DEVICE_ID_EMULATOR_3 = "3CFEF5EDED2F2CC6C866A48114EA2ECE";

    public ApplicationContext getApplicationContext() {
        if(applicationContext == null) {
            try {
                applicationContext = (ApplicationContext) getActivity().getApplication();
            } catch (Exception e) {
                applicationContext = new ApplicationContext();
            }
        }
        return applicationContext;
    }
}
