package com.arjanvlek.cyngnotainfo;

import android.app.Activity;
import android.content.Intent;

import com.arjanvlek.cyngnotainfo.view.AboutActivity;
import com.arjanvlek.cyngnotainfo.view.FAQActivity;
import com.arjanvlek.cyngnotainfo.view.HelpActivity;
import com.arjanvlek.cyngnotainfo.view.SettingsActivity;
import com.arjanvlek.cyngnotainfo.view.SetupActivity;
import com.arjanvlek.cyngnotainfo.view.InstallGuideActivity;

import static com.arjanvlek.cyngnotainfo.view.InstallGuideActivity.INTENT_SHOW_DOWNLOAD_PAGE;

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
    public void UpdateInstructions(boolean isDownloaded) {
        Intent i = new Intent(baseActivity, InstallGuideActivity.class);
        i.putExtra(INTENT_SHOW_DOWNLOAD_PAGE, (!isDownloaded));
        baseActivity.startActivity(i);
    }

    private <T> void startActivity(Class<T> activityClass) {
        Intent i = new Intent(baseActivity, activityClass);
        baseActivity.startActivity(i);
    }


}
