package com.arjanvlek.cyngnotainfo;

import android.test.ActivityInstrumentationTestCase2;

import com.arjanvlek.cyngnotainfo.Support.ServerConnector;
import com.arjanvlek.cyngnotainfo.views.UpdateInstallationGuideActivity;
import com.robotium.solo.Solo;

import java.util.Locale;

public class UpdateInstallationGuideTest extends ActivityInstrumentationTestCase2<UpdateInstallationGuideActivity>{

    private Solo solo;

    public UpdateInstallationGuideTest() { super(UpdateInstallationGuideActivity.class); }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }


    public void testInstallationGuide() throws Exception {
        UpdateInstallationGuideActivity activity = getActivity();
        String appLocale = Locale.getDefault().getDisplayLanguage();

        // Letting Solo click on a text means it's there. If it is not found, an exception is thrown.
        // Therefore, no asserts are needed here.

        solo.clickOnText(activity.getString(R.string.install_guide_step1_explanation));

        swipeToRight();

        solo.clickOnText(activity.getString(R.string.install_guide_step2_press_buttons));

        swipeToRight();

        solo.clickOnText(activity.getString(R.string.install_guide_step3_apply_update));

        swipeToRight();

        solo.clickOnText(activity.getString(R.string.install_guide_step4_cyanogen_logo));

        swipeToRight();

        solo.clickOnText(activity.getString(R.string.install_guide_step5_check_version));

        switch (appLocale) {
            case "Nederlands":
                solo.clickOnButton("Sluit de installatiehandleiding");
                break;
            default:
                solo.clickOnButton("Close Installation Guide");
                break;
        }



    }



    private void swipeToRight() {
        solo.scrollToSide(Solo.RIGHT);
        solo.sleep(1500);

    }
}
