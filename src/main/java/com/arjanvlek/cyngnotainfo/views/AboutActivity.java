package com.arjanvlek.cyngnotainfo.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.arjanvlek.cyngnotainfo.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);
        String versionNumber = BuildConfig.VERSION_NAME;
        setContentView(R.layout.activity_about);
        TextView versionNumberView = (TextView) findViewById(R.id.aboutVersionNumberView);
        versionNumberView.setText(getString(R.string.about_version) + " " + versionNumber);

        //Make link clickable
        TextView storyView = (TextView) findViewById(R.id.aboutBackgroundStoryView);
        storyView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void rateApp(View view) {
        try {
            final String appPackageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        } catch (Exception ignored) {

        }
    }

}
