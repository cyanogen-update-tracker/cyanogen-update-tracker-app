package com.arjanvlek.cyngnotainfo;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Spinner;

import com.arjanvlek.cyngnotainfo.Support.ServerConnector;
import com.arjanvlek.cyngnotainfo.views.SettingsActivity;
import com.robotium.solo.Solo;

import java.util.Locale;

public class UAPrepareTest extends ActivityInstrumentationTestCase2<SettingsActivity> {

    private Solo solo;

    public UAPrepareTest() { super(SettingsActivity.class);}

    @Override
    public void setUp() throws Exception{
        ServerConnector.testing = true;
        GcmRegistrationIntentService.testing = true;
        solo = new Solo(getInstrumentation(), getActivity());
        String appLocale = Locale.getDefault().getDisplayLanguage();
        Spinner deviceSpinner = (Spinner)getActivity().findViewById(R.id.settingsDeviceSpinner);
        Spinner updateMethodSpinner = (Spinner)getActivity().findViewById(R.id.settingsUpdateMethodSpinner);

        // Wait for app to launch
        solo.sleep(500);
        // Choose first test device with first update method
        solo.clickOnView(deviceSpinner);
        solo.clickOnText("Test Device 1");
        solo.clickOnView(updateMethodSpinner);
        switch(appLocale) {
            case "Nederlands":
                solo.clickOnText("Volledige testupdate");
                break;
            default:
                solo.clickOnText("Full test update");
                break;
        }
    }

    @Override
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

    public void testPrepared() throws Exception {
        assertTrue(true);
    }

}
