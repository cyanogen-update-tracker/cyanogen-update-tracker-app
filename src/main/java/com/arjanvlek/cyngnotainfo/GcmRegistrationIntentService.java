package com.arjanvlek.cyngnotainfo;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.arjanvlek.cyngnotainfo.Support.ServerConnector;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GcmRegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private String token = "";
    private String deviceType;
    private String updateType;

    //Settings properties
    public static final String PROPERTY_DEVICE_TYPE = "device_type";
    public static final String PROPERTY_UPDATE_TYPE = "update_type";
    public static final String PROPERTY_GCM_REG_ID = "registration_id";
    public static final String PROPERTY_GCM_DEVICE_TYPE = "gcm_device_type";
    public static final String PROPERTY_GCM_UPDATE_TYPE = "gcm_update_type";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String PROPERTY_REGISTRATION_ERROR = "registration_error";

    //JSON Properties
    private static final String JSON_PROPERTY_DEVICE_REGISTRATION_ID = "device_id";
    private static final String JSON_PROPERTY_DEVICE_TYPE = "tracking_device_type";
    private static final String JSON_PROPERTY_UPDATE_TYPE = "tracking_update_type";
    private static final String JSON_PROPERTY_OLD_DEVICE_ID = "old_device_id";

    //Server URLs
    public static String SERVER_URL = "** Add the base URL of your API / backend here **register-device.php";
    public static String TEST_SERVER_URL = "http://192.168.178.14/register-device.php";

    public GcmRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        deviceType = MainActivity.getPreference(PROPERTY_DEVICE_TYPE, getApplicationContext());
        updateType = MainActivity.getPreference(PROPERTY_UPDATE_TYPE, getApplicationContext());

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                InstanceID instanceID = InstanceID.getInstance(this);
                token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                sendRegistrationToServer(token);
            }
        } catch (Exception e) {
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            setRegistrationFailed(true);
        }
        //Release the wake lock received when the app has been upgraded.
        if(intent.getExtras() != null) {
            try {
                if (intent.getExtras().getBoolean("package_upgrade", false)) {
                    GcmPackageReplacedReceiver.completeWakefulIntent(intent);
                }
            } catch (Exception ignored) {

            }
        }
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * This code connects to <a href="arjan1995.raspctl.com">arjan1995.raspctl.com</a> to store the device registration.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new RegisterIdToBackend().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, token, getCurrentRegistrationId());
        } else {
            new RegisterIdToBackend().execute(token, getCurrentRegistrationId());
        }
    }

    /**
     * Registers a GCM Registration Token to the app's server.
     */
    private class RegisterIdToBackend extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            String registrationId = strings[0];
            String oldRegistrationId = strings[1];
            HttpURLConnection urlConnection = null;
            InputStream in;
            String result = null;
            try {

                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put(JSON_PROPERTY_DEVICE_REGISTRATION_ID, registrationId);
                jsonResponse.put(JSON_PROPERTY_DEVICE_TYPE, deviceType);
                jsonResponse.put(JSON_PROPERTY_UPDATE_TYPE, updateType);
                jsonResponse.put(JSON_PROPERTY_OLD_DEVICE_ID, oldRegistrationId);
                URL url = new URL(SERVER_URL);
//                URL url = new URL(TEST_SERVER_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("User-Agent", ServerConnector.USER_AGENT);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                urlConnection.connect();
                OutputStream out = urlConnection.getOutputStream();
                byte[] outputBytes = jsonResponse.toString().getBytes();
                out.write(outputBytes);
                out.close();
                in = new BufferedInputStream(urlConnection.getInputStream());
                result = inputStreamToString(in);
            } catch (Exception e) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String response) {
            JSONObject result;
            try {
                result = new JSONObject(response);
                System.out.println(result.toString());
                if (result.getString("success") != null) {
                    setRegistrationFailed(false);
                    storeRegistrationId(getApplicationContext(),token);
                } else {
                    setRegistrationFailed(true);
                }
            } catch (Exception e) {
                setRegistrationFailed(true);
            }
        }
    }

    /**
     * Sets a flag that this device needs to be re-registered for push notifications at a later stage.
     * @param failed Returns if GCM Registration has failed.
     */
    private void setRegistrationFailed(boolean failed) {
        SharedPreferences preferences = getGCMPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PROPERTY_REGISTRATION_ERROR, failed);
        if (failed) {
            try {
                Toast.makeText(this, getString(R.string.push_failure), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                try {
                    Toast.makeText(this, getString(R.string.push_failure), Toast.LENGTH_LONG).show();
                } catch(Exception ignored) {

                }
            }
        }
        editor.apply();
    }

    /**
     * Returns the current device registration token, used for push notifications.
     * @return GCM Registration Token.
     */
    private String getCurrentRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_GCM_REG_ID, "");
        if (registrationId != null && registrationId.isEmpty()) {
            return "";
        }
        return registrationId;
    }

    /**
     * Returns the special section of settings that contains the notification settings.
     * @return GCM Shared Preferences.
     */
    private SharedPreferences getGCMPreferences() {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }


    /**
     * Converts an InputStream (e.g. from HttpUrlConnection) to a normal String.
     * @param in InputStream that will be converted
     * @return String with the same text as in the InputStream.
     */
    private String inputStreamToString(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }



    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_GCM_REG_ID, regId);
        editor.putString(PROPERTY_GCM_DEVICE_TYPE, deviceType);
        editor.putString(PROPERTY_GCM_UPDATE_TYPE, updateType);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
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

}
