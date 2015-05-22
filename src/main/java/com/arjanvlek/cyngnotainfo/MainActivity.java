package com.arjanvlek.cyngnotainfo;

import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.arjanvlek.cyngnotainfo.Settings.DeviceSettingsFragment;
import com.arjanvlek.cyngnotainfo.Settings.UpdateSettingsFragment;
import com.arjanvlek.cyngnotainfo.views.AboutActivity;
import com.arjanvlek.cyngnotainfo.views.DeviceInformationFragment;
import com.arjanvlek.cyngnotainfo.views.UpdateInformationFragment;
import com.arjanvlek.cyngnotainfo.views.UpdateInstallationInstructionsActivity;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity implements ActionBar.TabListener, GoogleApiClient.OnConnectionFailedListener {

    private ViewPager mViewPager;
    private AdView mAdView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);

        SharedPreferences preferences = getPreferences(MODE_APPEND);
        String deviceName = preferences.getString("device-name", "not-set");
        String updateType = preferences.getString("update-type", "not-set");
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }


        if(updateType != null) {
            if (updateType.equals("not-set")) {
                askForUpdateSettings();
            }

        }
        if (deviceName != null) {
            if(deviceName.equals("not-set")) {
                askForDeviceSettings();
            }
        }

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (actionBar != null) {
                    actionBar.setSelectedNavigationItem(position);
                }
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    private void askForDeviceSettings() {
            DialogFragment newFragment = new DeviceSettingsFragment();
            newFragment.show(getSupportFragmentManager(), "deviceSettings");


    }

    private void askForUpdateSettings() {
        DialogFragment fragment = new UpdateSettingsFragment();
        fragment.show(getSupportFragmentManager(), "updateSettings");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }
    public void Settings() {
        askForUpdateSettings();
        askForDeviceSettings();

    }

    private void About() {
        Intent i = new Intent(this,AboutActivity.class);
        startActivity(i);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Settings();
            return true;
        }
        if (id == R.id.action_about) {
            About();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a FragmentBuilder (defined as a static inner class below).
            return FragmentBuilder.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * An inner class that constructs the fragments used in this application.
     */
    public static class FragmentBuilder {

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static Fragment newInstance(int sectionNumber) {
            if(sectionNumber == 1) {
                return new UpdateInformationFragment();
            }
            if(sectionNumber == 2) {
                return new DeviceInformationFragment();
            }
            return null;
        }

    }
    public void showUpdateInstructions(View v){
        Intent i = new Intent(this,UpdateInstallationInstructionsActivity.class);
        startActivity(i);
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

}
