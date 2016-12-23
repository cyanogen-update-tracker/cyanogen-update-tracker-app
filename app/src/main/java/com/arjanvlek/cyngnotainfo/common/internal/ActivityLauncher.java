package com.arjanvlek.cyngnotainfo.common.internal;

import android.app.Activity;
import android.content.Intent;

import com.arjanvlek.cyngnotainfo.common.activity.AboutActivity;
import com.arjanvlek.cyngnotainfo.common.activity.FAQActivity;
import com.arjanvlek.cyngnotainfo.common.activity.HelpActivity;
import com.arjanvlek.cyngnotainfo.common.activity.SettingsActivity;
import com.arjanvlek.cyngnotainfo.common.activity.SetupActivity;
import com.arjanvlek.cyngnotainfo.cos.activity.InstallGuideActivity;

public class ActivityLauncher {

    private final Activity baseActivity;

    public ActivityLauncher(Activity baseActivity) {
        this.baseActivity = baseActivity;
    }


    /**
     * Opens the settings page.
     */
    public void Settings() {
        startActivity(SettingsActivity.class);
    }

    /**
     * Opens the welcome tutorial.
     */
    public void Tutorial() {
        startActivity(SetupActivity.class);
    }

    /**
     * Opens the about page.
     */
    public void About() {
        startActivity(AboutActivity.class);
    }

    /**
     * Opens the help page.
     */
    public void Help() {
        startActivity(HelpActivity.class);
    }

    /**
     * Opens the faq page.
     */
    public void FAQ() {
        startActivity(FAQActivity.class);
    }


    /**
     * Opens the update instructions page.
     */
    public void UpdateInstructions() {
        startActivity(InstallGuideActivity.class);
    }

    private <T> void startActivity(Class<T> activityClass) {
        Intent i = new Intent(this.baseActivity, activityClass);
        this.baseActivity.startActivity(i);
    }


}
