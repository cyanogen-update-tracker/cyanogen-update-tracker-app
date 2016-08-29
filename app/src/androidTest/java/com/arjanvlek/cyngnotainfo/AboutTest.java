package com.arjanvlek.cyngnotainfo;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.views.AboutActivity;
import com.robotium.solo.Solo;

import java.util.Locale;

public class AboutTest extends ActivityInstrumentationTestCase2<AboutActivity> {

    private Solo solo;
    public final static String LOCALE_DUTCH = "Nederlands";

    public AboutTest() {
        super(AboutActivity.class);
    }



    @Override
    public void setUp() throws Exception{
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

    public void testAboutScreen() throws Exception {
        String appLocale = Locale.getDefault().getDisplayLanguage();

        // Check if version number is displayed correctly
        TextView versionNumberView = (TextView)solo.getView(R.id.aboutVersionNumberView);

        assertEquals(View.VISIBLE, versionNumberView.getVisibility());

        String versionNumber;
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionNumber = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            fail("Error while trying to detect application version information. Package not found.");
            versionNumber = null;
        }

        if(appLocale.equals(LOCALE_DUTCH)) {
            assertEquals("Versie: " + versionNumber, versionNumberView.getText());
        } else {
            assertEquals("Version: " + versionNumber, versionNumberView.getText());
        }

        // Check if the about line is displayed correctly
        String aboutDescription = getActivity().getString(R.string.about_app_description);
        TextView aboutDescriptionView = (TextView)solo.getView(R.id.aboutSlogan);

        assertEquals(View.VISIBLE, aboutDescriptionView.getVisibility());
        assertEquals(aboutDescription, aboutDescriptionView.getText());

        // Check if the about story is displayed correctly
        String aboutStoryText = getActivity().getString(R.string.about_background_story).substring(0,100);
        TextView aboutStoryView = (TextView)solo.getView(R.id.aboutBackgroundStoryView);

        assertEquals(View.VISIBLE, aboutStoryView.getVisibility());
        assertEquals(aboutStoryText, aboutStoryView.getText().toString().substring(0,100));
    }
}
