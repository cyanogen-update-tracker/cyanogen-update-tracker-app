package com.arjanvlek.cyngnotainfo.Support;

import android.os.AsyncTask;

import com.arjanvlek.cyngnotainfo.BuildConfig;

import org.apache.http.HttpException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by Arjan on 29-5-2015. Part of Cyanogen Update Tracker.
 */
public class DatabaseHelper {

    private final String USER_AGENT = "Cyanogen_update_tracker_" + BuildConfig.VERSION_NAME;
    private final String SERVER_URL = "** Add the base URL of your API / backend here **";
    private final String DEVICE_TYPE_URL = "check_device_types.php";
    private final String UPDATE_TYPE_URL = "check_update_types.php";
    private Map<String, Long> deviceTypes;
    private Map<String, Long> updateTypes;
    boolean deviceTypesReady = false;
    boolean updateTypesReady = false;


    public DatabaseHelper() {
        new fetchDataFromServer().execute("device");
        new fetchDataFromServer().execute("update");
    }
    public Map<String,Long> getDeviceTypes() {
        if(deviceTypesReady) {
            return deviceTypes;
        }
        else {
            return null;
        }
    }

    public Map<String, Long> getUpdateTypes() {
        if(updateTypesReady) {
            return updateTypes;
        }
        else {
            return null;
        }
    }

    private void findAllDeviceTypesFromHtmlResponse(String htmlResponse) {
        deviceTypesReady = true;
    }


    private void findAllUpdateTypesFromHtmlResponse(String htmlResponse) {
        updateTypesReady = true;
    }


    private class fetchDataFromServer extends AsyncTask<String,Void,String[]> {

        @Override
        protected String[] doInBackground(String... types) {
            String type = types[0];

            URL requestUrl = null;
            try {
                switch(type) {
                    case "device":
                        requestUrl = new URL(SERVER_URL + DEVICE_TYPE_URL);
                        break;
                    case "update":
                        requestUrl = new URL(SERVER_URL + UPDATE_TYPE_URL);
                        break;
                    default:
                        return null;

                }
                HttpURLConnection urlConnection = (HttpURLConnection) requestUrl.openConnection();

                //setup request
                urlConnection.setRequestProperty("User-Agent", USER_AGENT);
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);

                int responseCode = urlConnection.getResponseCode();
                if (responseCode <= 300) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    String[] typeAndHtmlResponse = new String[2];
                    typeAndHtmlResponse[0] = type;
                    typeAndHtmlResponse[1] = response.toString();
                    in.close();
                    return typeAndHtmlResponse;


                } else {
                    throw new HttpException("Eror occurred during data fetch from server. server response code: " + responseCode + "." );
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] typeAndHtmlResponse) {
            String type = typeAndHtmlResponse[0];
            if (type.equals("device")) {
                findAllDeviceTypesFromHtmlResponse(typeAndHtmlResponse[1]);
            } else if (type.equals("update")) {
                findAllUpdateTypesFromHtmlResponse(typeAndHtmlResponse[1]);
            }
        }

    }






}

