package com.arjanvlek.cyngnotainfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.Support.ServerConnector;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;
import com.google.android.gms.ads.AdView;
import com.robotium.solo.Solo;

/**
 * Tests the Update Information Screen.
 */
public class UpdateInformationTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo;
    public final static String UPDATE_NAME = "TestData1";
    public final static String DOWNLOAD_SIZE = "570 MB";

    public UpdateInformationTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception{
        ServerConnector.testing = true;
        GcmRegistrationIntentService.testing = true;
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

    public void testUpdateInformation() throws Exception {

        // Setting up the test
        SharedPreferences gcmPreferences = getActivity().getApplicationContext().getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);


        // sometimes the screen doesn't go back, so try it again if the test fails
        try {
            // test the update name in the top
            TextView buildNumberText = (TextView)solo.getView(R.id.updateInformationBuildNumberView);
            assertEquals(UPDATE_NAME, buildNumberText.getText());
        }
        catch (Exception e) {
            solo.goBack();
            solo.sleep(1000);
            TextView buildNumberText = (TextView)solo.getView(R.id.updateInformationBuildNumberView);
            assertEquals(UPDATE_NAME, buildNumberText.getText());

        }

        // test the download size
        TextView downloadSizeText = (TextView)solo.getView(R.id.updateInformationDownloadSizeView);
        assertEquals(DOWNLOAD_SIZE, downloadSizeText.getText());


        // test if app has registered successfully for push notifications
        ServerConnector.testing = true;
        assertTrue(gcmPreferences.contains(SettingsManager.PROPERTY_REGISTRATION_ERROR));
        assertFalse(gcmPreferences.getBoolean(SettingsManager.PROPERTY_REGISTRATION_ERROR, false));

        AdView adView = (AdView) solo.getView(R.id.updateInformationAdView);
        assertEquals(getActivity().getString(R.string.update_information_advertising_id), adView.getAdUnitId());


        // test if the app responds normally to screen rotation
//        for(int i = 0; i< 5; i++) {
//            solo.setActivityOrientation(Solo.LANDSCAPE);
//            solo.setActivityOrientation(Solo.PORTRAIT);
//        }

//        // test if the app responds properly without network connection
//        solo.setMobileData(false);
//        solo.setWiFiData(false);
//        // swipe down to refresh
//        solo.drag(150, 150, 200, 500, 100);
//        View noConnectionView  = solo.getView(R.id.updateInformationNoConnectionBar);
//        assertEquals(View.VISIBLE, noConnectionView.getVisibility());
    }
}
