package com.arjanvlek.cyngnotainfo;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.arjanvlek.cyngnotainfo.views.AboutActivity;
import com.arjanvlek.cyngnotainfo.views.FAQActivity;
import com.arjanvlek.cyngnotainfo.views.HelpActivity;
import com.arjanvlek.cyngnotainfo.views.SettingsActivity;
import com.arjanvlek.cyngnotainfo.views.TutorialActivity;
import com.arjanvlek.cyngnotainfo.views.UpdateInstallationGuideActivity;

public class ActivityLauncher {

    private final Activity baseActivity;

    public ActivityLauncher(Activity baseActivity) {
        this.baseActivity = baseActivity;
    }


    /**
     * Opens the settings page.
     */
    protected void Settings() {
        startActivity(SettingsActivity.class);
    }

    /**
     * Opens the welcome tutorial.
     */
    protected void Tutorial() {
        startActivity(TutorialActivity.class);
    }

    /**
     * Opens the about page.
     */
    protected void About() {
        startActivity(AboutActivity.class);
    }

    /**
     * Opens the help page.
     */
    protected void Help() {
        startActivity(HelpActivity.class);
    }

    /**
     * Opens the faq page.
     */
    protected void FAQ() {
        startActivity(FAQActivity.class);
    }


    /**
     * Opens the update instructions page.
     */
    public void UpdateInstructions() {
        startActivity(UpdateInstallationGuideActivity.class);
    }

    private <T> void startActivity(Class<T> activityClass) {
        Intent i = new Intent(baseActivity, activityClass);
        baseActivity.startActivity(i);
    }


}
