package com.arjanvlek.cyngnotainfo.views;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.Model.DeviceInformationData;
import com.arjanvlek.cyngnotainfo.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);
        String versionNumber = null;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionNumber = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_about);
        TextView versionNumberView = (TextView)findViewById(R.id.about_version_number_field);
        if(versionNumber == null) {
            versionNumber = DeviceInformationData.UNKNOWN;
        }
        versionNumberView.setText(getString(R.string.version) + " " + versionNumber);

        //Make link clickable
        TextView storyView = (TextView)findViewById(R.id.about_story_field);
        storyView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void rateApp(View view) {
        try {
            final String appPackageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
        catch(Exception ignored) {
            
        }
    }

    public void close (View view) {
        finish();
    }

}
