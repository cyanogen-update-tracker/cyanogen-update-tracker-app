package com.arjanvlek.cyngnotainfo;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Spinner;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.Support.ServerConnector;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;
import com.arjanvlek.cyngnotainfo.views.TutorialActivity;
import com.robotium.solo.Solo;

import java.util.Locale;

import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.*;

public class TutorialTest extends ActivityInstrumentationTestCase2<TutorialActivity> {

    private Solo solo;
    public final static String LOCALE_DUTCH = "Nederlands";

    public TutorialTest() {
        super(TutorialActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        ServerConnector.testing = true;
        GcmRegistrationIntentService.testing = true;
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testTutorial() throws Exception {
        TutorialActivity activity = getActivity();
        SettingsManager settingsManager = new SettingsManager(getActivity().getApplicationContext());
        String appLocale = Locale.getDefault().getDisplayLanguage();

        // Test first screen
        TextView page1Text = (TextView)activity.findViewById(R.id.introduction_step_1_header_text);
        assertEquals(activity.getString(R.string.introduction_welcome), page1Text.getText());
        solo.sleep(1000);

        swipeToRight();
        solo.sleep(1500);

        // Test second screen
        solo.clickOnText(activity.getString(R.string.help_how_works_app));

        swipeToRight();
        solo.sleep(1000);

        // Test third screen
        // Opens spinner
        solo.clickOnText("Test Device 1");

        // Selects second spinner option
        solo.clickOnText("Test Device 2");
        // wait for settings to save
        solo.sleep(300);
        assertEquals("Test Device 2", settingsManager.getPreference(PROPERTY_DEVICE));

        swipeToRight();
        solo.sleep(1500);

        // Test fourth screen
        Spinner page4Spinner = (Spinner)activity.findViewById(R.id.settingsUpdateMethodSpinner);
        solo.clickOnView(page4Spinner);
        switch(appLocale) {
            case LOCALE_DUTCH:
                solo.clickOnText("Incrementele testupdate");
                break;
            default:
                solo.clickOnText("Incremental test update");
                break;

        }
        // wait for settings to save
        solo.sleep(300);

        switch (appLocale) {
            case LOCALE_DUTCH:
                assertEquals("Incrementele testupdate", settingsManager.getPreference(PROPERTY_UPDATE_METHOD));
                break;
            default:
                assertEquals("Incremental test update", settingsManager.getPreference(PROPERTY_UPDATE_METHOD));
        }

        swipeToRight();
        solo.sleep(1500);

        // Test last page
        solo.clickOnText(activity.getString(R.string.introduction_ready_to_start));

        switch (appLocale) {
            case LOCALE_DUTCH:
                solo.clickOnButton("start de app");
                break;
            default:
                solo.clickOnButton("start app");
                break;
        }
    }

    private void swipeToRight() {
        solo.scrollToSide(Solo.RIGHT);
    }
}