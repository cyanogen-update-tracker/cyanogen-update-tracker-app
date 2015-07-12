package com.arjanvlek.cyngnotainfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.Support.ServerConnector;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;
import com.github.mikephil.charting.charts.PieChart;
import com.robotium.solo.Solo;

import java.util.Locale;

/**
 * Tests the Update Information Screen.
 */
public class UpdateInformationTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo;
    public final static String UPDATE_NAME = "CyanogenTest";
    public final static String DEVICE_NAME = "Test Device";
    public final static String DOWNLOAD_SIZE = "570 MB";
    public final static String UPDATED_TIME = "6/6/2015 at 11:02 PM";
    public final static String UPDATED_TIME_DUTCH = "06-06-2015 om 23:02";
    public final static String ROLL_OUT_PERCENTAGE = "95%";
    public final static String LOCALE_DUTCH = "Nederlands";

    public UpdateInformationTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception{
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

    public void testUpdateInformation() throws Exception {

        // Setting up the test
        ServerConnector.testing = true;
        GcmRegistrationIntentService.testing = true;
        String appLocale = Locale.getDefault().getDisplayLanguage();
        SharedPreferences gcmPreferences = getActivity().getApplicationContext().getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);


        if(appLocale.equals(LOCALE_DUTCH)) {
            //Kies het testapparaat in Instellingen
            solo.clickOnMenuItem("Instellingen");
            solo.sleep(3000);
            solo.goBack();
            solo.sleep(1000);
        } else {
            //Choose test device in Settings
            solo.clickOnMenuItem("Settings");
            solo.sleep(3000);
            solo.goBack();
            solo.sleep(1000);
        }
        // sometimes the screen doesn't go back, so try it again if the test fails
        try {
            // test the update name in the top
            TextView buildNumberText = (TextView)solo.getView(R.id.buildNumberLabel);
            if(appLocale.equals(LOCALE_DUTCH)) {
                assertEquals(UPDATE_NAME + " voor " + DEVICE_NAME, buildNumberText.getText());
            } else {
                assertEquals(UPDATE_NAME + " for " + DEVICE_NAME, buildNumberText.getText());
            }
        }
        catch (Exception e) {
            solo.goBack();
            solo.sleep(1000);
            TextView buildNumberText = (TextView)solo.getView(R.id.buildNumberLabel);
            if(appLocale.equals(LOCALE_DUTCH)) {
                assertEquals(UPDATE_NAME + " voor " + DEVICE_NAME, buildNumberText.getText());
            } else {
                assertEquals(UPDATE_NAME + " for " + DEVICE_NAME, buildNumberText.getText());
            }
        }

        // test the download size
        TextView downloadSizeText = (TextView)solo.getView(R.id.downloadSizeLabel);
        assertEquals(DOWNLOAD_SIZE, downloadSizeText.getText());

        //test the last updated time
        TextView lastUpdatedText = (TextView)solo.getView(R.id.lastUpdatedLabel);
        if(appLocale.equals(LOCALE_DUTCH)) {
            assertEquals(UPDATED_TIME_DUTCH, lastUpdatedText.getText());
        }
        else {
            assertEquals(UPDATED_TIME, lastUpdatedText.getText());
        }

        // test the roll out percentage
        PieChart pieChart = (PieChart)solo.getView(R.id.rolloutPercentageDiagram);
        assertEquals(ROLL_OUT_PERCENTAGE, pieChart.getCenterText());

        // test if app has registered successfully for push notifications
        ServerConnector.testing = true;
        assertTrue(gcmPreferences.contains(SettingsManager.PROPERTY_REGISTRATION_ERROR));
        assertFalse(gcmPreferences.getBoolean(SettingsManager.PROPERTY_REGISTRATION_ERROR, false));

        // test if the app responds normally to screen rotation
        for(int i = 0; i< 5; i++) {
            solo.setActivityOrientation(Solo.LANDSCAPE);
            solo.setActivityOrientation(Solo.PORTRAIT);
        }

//        // test if the app responds properly without network connection
//        solo.setMobileData(false);
//        solo.setWiFiData(false);
//        // swipe down to refresh
//        solo.drag(150, 150, 200, 500, 100);
//        View noConnectionView  = solo.getView(R.id.updateInformationNoConnectionBar);
//        assertEquals(View.VISIBLE, noConnectionView.getVisibility());
    }
}
