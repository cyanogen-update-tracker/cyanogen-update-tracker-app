package com.arjanvlek.cyngnotainfo;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class MainActivity extends AppCompatActivity implements ActionBar.TabListener, GoogleApiClient.OnConnectionFailedListener {

    private ViewPager mViewPager;
    private AdView mAdView;

    // Used for Google Play Services check
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // Used to register for Push Notifications
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private String SENDER_ID = "** Add your Google Cloud Messaging API key here **";
    private GoogleCloudMessaging cloudMessaging;
    AtomicInteger msgId = new AtomicInteger();
    private SharedPreferences messagingPreferences;
    private Context context;
    private String registrationId;
    private String deviceName = "";
    private String updateType = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);
        context = getApplicationContext();
        if (checkPlayServices()) {
            cloudMessaging = GoogleCloudMessaging.getInstance(context);
            registrationId = getRegistrationId(context); //TODO write

            SharedPreferences preferences = getPreferences(MODE_APPEND);
            deviceName = preferences.getString("device-name", "not-set");
            updateType = preferences.getString("update-type", "not-set");
//            if(registrationId.isEmpty() && !deviceName.equals("not-set")) {
                registerInBackground(); //TODO write
//            }

            // Set up the action bar.
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            }


            if (updateType != null) {
                if (updateType.equals("not-set")) {
                    askForUpdateSettings();
                }

            }
            if (deviceName != null) {
                if (deviceName.equals("not-set")) {
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
        else {
            System.out.println("No Google Play Services found :-(");
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
        checkPlayServices();
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

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                System.out.println("This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId != null && registrationId.isEmpty()) {
            System.out.println("Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            System.out.println("App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences() {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }



    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new cloudRegisterTask().execute(null, null, null);
    }


    private class cloudRegisterTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... objects) {
            try {
                if (cloudMessaging == null) {
                    cloudMessaging = GoogleCloudMessaging.getInstance(context);
                }
                registrationId = cloudMessaging.register(SENDER_ID);

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
                sendRegistrationIdToBackend(registrationId);

                // For this demo: we don't need to send it because the device
                // will send upstream messages to a server that echo back the
                // message using the 'from' address in the message.

                // Persist the registration ID - no need to register again.
                storeRegistrationId(context, registrationId);
            } catch (IOException ex) {
                ex.printStackTrace();
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }
            return null;
        }

    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend(String registrationId) {
        new RegisterIdToBackend().execute(registrationId);
    }

    private class RegisterIdToBackend extends AsyncTask<String,Integer, HttpResponse<JsonNode>> {

        @Override
        protected HttpResponse<JsonNode> doInBackground(String... strings) {
            String regId = strings[0];
            HttpResponse<JsonNode> jsonResponse = null;
            try {
                jsonResponse = Unirest.post("** Add the base URL of your API / backend here **register_device.php")
                        .header("accept", "application/json")
                        .field("tracking_device_type", deviceName )
                        .field("tracking_update_type", updateType)
                        .field("device_id", regId)
                        .asJson(); //TODO fix request
            } catch (UnirestException e) {
                e.printStackTrace();
            }
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(HttpResponse<JsonNode> response) {
            try {
                System.out.println("response status code: " + response.getStatusText());
                JsonNode result = response.getBody();
                System.out.println("response: " + result.toString()); //TODO filter server errors
            }
            catch (Exception e) {
                e.printStackTrace();

            }

        }
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(context);
        System.out.println("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }
}
