package com.arjanvlek.cyngnotainfo;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Spinner;

import com.arjanvlek.cyngnotainfo.Support.ServerConnector;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;
import com.arjanvlek.cyngnotainfo.views.SettingsActivity;
import com.robotium.solo.Solo;

import junit.framework.AssertionFailedError;

import java.util.Locale;

import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.*;

public class SettingsTest extends ActivityInstrumentationTestCase2<SettingsActivity> {
    private Solo solo;
    public final static String LOCALE_DUTCH = "Nederlands";

    public SettingsTest() {
        super(SettingsActivity.class);
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

    public void testDeviceSettings() throws Exception{

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
            case LOCALE_DUTCH:
                solo.clickOnText("Incrementele testupdate");
                break;
            default:
                solo.clickOnText("Incremental test update");
                break;
        }

        // Wait for settings to save
        solo.sleep(1000);

        SettingsManager settingsManager = new SettingsManager(getActivity().getApplicationContext());

        // Test if everything is set correctly.
        assertEquals(1L, settingsManager.getLongPreference(PROPERTY_DEVICE_ID));
        assertEquals("Test Device 1", settingsManager.getPreference(PROPERTY_DEVICE));
        assertEquals(1L, settingsManager.getLongPreference(PROPERTY_UPDATE_METHOD_ID));
        switch(appLocale) {
            case LOCALE_DUTCH:
                assertEquals("Incrementele testupdate", settingsManager.getPreference(PROPERTY_UPDATE_METHOD));
                break;
            default:
                assertEquals("Incremental test update", settingsManager.getPreference(PROPERTY_UPDATE_METHOD));
                break;
        }
        assertEquals("http://cyanogenupdatetracker.com/test/api/v1/testUpdateData2.json", settingsManager.getPreference(PROPERTY_UPDATE_DATA_LINK));

        // Choose second test device
        solo.clickOnView(deviceSpinner);
        solo.clickOnText("Test Device 2");
        solo.clickOnView(updateMethodSpinner);
        switch(appLocale) {
            case LOCALE_DUTCH:
                solo.clickOnText("Incrementele testupdate");
                break;
            default:
                solo.clickOnText("Incremental test update");
                break;
        }
        solo.sleep(1000);

        // Assert all settings
        assertEquals(2L, settingsManager.getLongPreference(PROPERTY_DEVICE_ID));
        assertEquals("Test Device 2", settingsManager.getPreference(PROPERTY_DEVICE));
        assertEquals(1L, settingsManager.getLongPreference(PROPERTY_UPDATE_METHOD_ID));
        switch(appLocale) {
            case LOCALE_DUTCH:
                assertEquals("Incrementele testupdate", settingsManager.getPreference(PROPERTY_UPDATE_METHOD));
                break;
            default:
                assertEquals("Incremental test update", settingsManager.getPreference(PROPERTY_UPDATE_METHOD));
                break;
        }
        assertEquals("http://cyanogenupdatetracker.com/test/api/v1/testUpdateData2.json", settingsManager.getPreference(PROPERTY_UPDATE_DATA_LINK));

        // Choose first device with second update method
        solo.clickOnView(deviceSpinner);
        solo.clickOnText("Test Device 1");
        solo.sleep(200);
        solo.clickOnView(updateMethodSpinner);
        switch(appLocale) {
            case LOCALE_DUTCH:
                solo.clickOnText("Volledige testupdate");
                break;
            default:
                solo.clickOnText("Full test update");
                break;
        }
        // Wait for settings to save
        solo.sleep(1000);

        // Assert all settings
        assertEquals(1L, settingsManager.getLongPreference(PROPERTY_DEVICE_ID));
        assertEquals("Test Device 1", settingsManager.getPreference(PROPERTY_DEVICE));
        assertEquals(2L, settingsManager.getLongPreference(PROPERTY_UPDATE_METHOD_ID));
        switch(appLocale) {
            case LOCALE_DUTCH:
                assertEquals("Volledige testupdate", settingsManager.getPreference(PROPERTY_UPDATE_METHOD));
                break;
            default:
                assertEquals("Full test update", settingsManager.getPreference(PROPERTY_UPDATE_METHOD));
                break;
        }
        assertEquals("http://cyanogenupdatetracker.com/test/api/v1/testUpdateData1.json", settingsManager.getPreference(PROPERTY_UPDATE_DATA_LINK));

        // Choose second device with second update method (does not exist)
        solo.clickOnView(deviceSpinner);
        solo.clickOnText("Test Device 2");
        solo.sleep(200);
        solo.clickOnView(updateMethodSpinner);
        boolean exceptionThrown = false;
        switch(appLocale) {
            case LOCALE_DUTCH:
                try {
                    solo.clickOnText("Volledige testupdate");
                } catch (AssertionFailedError e) {
                    exceptionThrown = true;
                }
                assertTrue(exceptionThrown);
                break;
            default:
                try {
                    solo.clickOnText("Full test update");
                } catch (AssertionFailedError e) {
                    exceptionThrown = true;
                }
                assertTrue(exceptionThrown);
                break;
            }
        // Wait for settings to save
        solo.sleep(1000);

        // Assert all settings
        assertEquals(2L, settingsManager.getLongPreference(PROPERTY_DEVICE_ID));
        assertEquals("Test Device 2", settingsManager.getPreference(PROPERTY_DEVICE));
    }

}
