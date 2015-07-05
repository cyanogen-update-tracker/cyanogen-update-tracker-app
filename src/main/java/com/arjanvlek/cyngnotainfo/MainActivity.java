package com.arjanvlek.cyngnotainfo;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
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

import com.arjanvlek.cyngnotainfo.views.HelpActivity;
import com.arjanvlek.cyngnotainfo.views.SettingsActivity;
import com.arjanvlek.cyngnotainfo.views.AboutActivity;
import com.arjanvlek.cyngnotainfo.views.DeviceInformationFragment;
import com.arjanvlek.cyngnotainfo.views.TutorialActivity;
import com.arjanvlek.cyngnotainfo.views.UpdateInformationFragment;
import com.arjanvlek.cyngnotainfo.views.UpdateInstallationInstructionsActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements ActionBar.TabListener {

    private ViewPager mViewPager;

    // Used for Google Play Services check
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // Used to register for Push Notifications
    public static final String PROPERTY_GCM_REG_ID = "registration_id";
    public static final String PROPERTY_GCM_DEVICE_TYPE = "gcm_device_type";
    public static final String PROPERTY_GCM_UPDATE_TYPE = "gcm_update_type";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String PROPERTY_DEVICE_TYPE = "device_type";
    public static final String PROPERTY_UPDATE_METHOD = "update_type";
    public static final String PROPERTY_REGISTRATION_ERROR = "registration_error";
    public static final String PROPERTY_UPDATE_LINK = "update_link";

    //Settings properties
    public static final String FULL_UPDATE = "full_update";
    public static final String INCREMENTAL_UPDATE = "incremental_update";

    //Local variables
    private Context context;
    private String deviceType = "";
    private String updateMethod = "";
    private String updateLink = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);
        context = getApplicationContext();

        //Fetch currently selected device, update method and update link
        deviceType = getPreference(PROPERTY_DEVICE_TYPE, getApplicationContext());
        updateMethod = getPreference(PROPERTY_UPDATE_METHOD, getApplicationContext());
        updateLink = getPreference(PROPERTY_UPDATE_LINK, getApplicationContext());

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
        setTitle(getString(R.string.app_name));

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
            // Creates a tab with text corresponding to the page title defined by
            // the adapter.
            //noinspection ConstantConditions
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if Google Play services are installed on the device
        if (checkPlayServices()) {
            // Check if a device, update method and update link have been set
            if (checkIfDeviceIsSet()) {
                //Check if there was a server error during registration for push notifications.
                checkIfRegistrationHasFailed();
                //Check if app needs to re-register for push notifications (like after device type change etc.)
                if (!checkIfRegistrationIsValid(context) && checkNetworkConnection()) {
                    registerInBackground();
                }
            }
            //Show the welcome tutorial if no device has been set
            if (deviceType == null || updateMethod == null || updateLink == null) {
                Tutorial();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles action bar item clicks.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Settings();
            return true;
        }
        if (id == R.id.action_about) {
            About();
            return true;
        }

        if (id == R.id.action_help) {
            Help();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Action when clicked on a tab.
     * @param tab Tab which is selected
     * @param fragmentTransaction Android Fragment Transaction, unused here.
     */
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
            if (sectionNumber == 1) {
                return new UpdateInformationFragment();
            }
            if (sectionNumber == 2) {
                return new DeviceInformationFragment();
            }
            return null;
        }

    }

    /**
     * Opens the settings page.
     */
    private void Settings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    /**
     * Opens the welcome tutorial.
     */
    private void Tutorial() {
        Intent i = new Intent(this, TutorialActivity.class);
        startActivity(i);
    }

    /**
     * Opens the about page.
     */
    private void About() {
        Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }

    /**
     * Opens the help page.
     */
    private void Help() {
        Intent i = new Intent(this, HelpActivity.class);
        startActivity(i);
    }


    /**
     * Opens the update instructions page.
     * @param v View (button onclick from XML).
     */
    public void UpdateInstructions(View v) {
        Intent i = new Intent(this, UpdateInstallationInstructionsActivity.class);
        startActivity(i);
    }


    /**
     * Checks if the Google Play Services are installed on the device.
     * @return Returns if the Google Play Services are installed.
     */
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

    /**
     * Checks if the device has an active network connection
     * @return Returns if the device has an active network connection
     */
    private boolean checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Fetches the Google Cloud Messaging (GCM) preferences which are stored in a separate file.
     * @return Shared Preferences with GCM preferences.
     */
    private SharedPreferences getGCMPreferences() {
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
        } catch (PackageManager.NameNotFoundException ignored) {
            return 0;
        }
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        Intent intent = new Intent(this,GcmRegistrationIntentService.class);
        startService(intent);
    }

    /**
     * Checks if a device, update method and update link have been set.
     * @return if the application is set up properly.
     */
    private boolean checkIfDeviceIsSet() {
        return checkPreference(MainActivity.PROPERTY_DEVICE_TYPE, getApplicationContext()) && checkPreference((MainActivity.PROPERTY_UPDATE_METHOD), getApplicationContext()) && checkPreference((MainActivity.PROPERTY_UPDATE_LINK), getApplicationContext());
    }

    /**
     * Checks if the registration token for push notifications is still valid.
     * @param context Application context
     * @return returns if the registration token is valid.
     */
    private boolean checkIfRegistrationIsValid(Context context) {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_GCM_REG_ID, "");
        String registeredDeviceType = prefs.getString(PROPERTY_GCM_DEVICE_TYPE, "");
        String registeredUpdateType = prefs.getString(PROPERTY_GCM_UPDATE_TYPE, "");

        // The registration token is empty, so not valid.
        if (registrationId != null && registrationId.isEmpty()) {
            return false;
        }

        // The registration token does not match the registered device type.
        if (!deviceType.equals(registeredDeviceType)) {
            return false;
        }

        // The registration token does not match the registered update method.
        if (!updateMethod.equals(registeredUpdateType)) {
            return false;
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        return registeredVersion == currentVersion;
    }

    /**
     * Checks if the registration for push notifications has failed before.
     */
    private void checkIfRegistrationHasFailed() {
        SharedPreferences preferences = getGCMPreferences();
        if (preferences.getBoolean(PROPERTY_REGISTRATION_ERROR, false) && checkNetworkConnection()) {
            registerInBackground();
        }
    }


    /**
     * Saves a String preference to SharedPreferences.
     * @param key Preference Key
     * @param value Preference Value
     * @param context Application Context (getApplicationContext())
     */
    public static void savePreference(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Saves an Integer preference to SharedPreferences.
     * @param key Preference Key
     * @param value Preference Value
     * @param context Application Context (getApplicationContext())
     */
    public static void saveIntPreference(String key, int value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Checks if a certain preference is set.
     * @param key Preference Key
     * @param context Application Context (getApplicationContext())
     * @return Returns if the given key is stored in the preferences.
     */
    public static boolean checkPreference(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.contains(key);
    }

    /**
     * Get a String preference from Shared Preferences
     * @param key Preference Key
     * @param context Application Context (getApplicationContext())
     * @return Preference Value
     */
    public static String getPreference(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    /**
     * Get a String preference from Shared Preferences
     * @param key Preference Key
     * @param context Application Context (getApplicationContext())
     * @return Preference Value
     */
    public static int getIntPreference(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, 0);
    }
}
