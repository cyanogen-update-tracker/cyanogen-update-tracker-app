package com.arjanvlek.cyngnotainfo.Support;

import android.os.AsyncTask;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.arjanvlek.cyngnotainfo.Model.DeviceTypeEntity;
import com.arjanvlek.cyngnotainfo.Model.UpdateLinkEntity;
import com.arjanvlek.cyngnotainfo.Model.UpdateTypeEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("StatementWithEmptyBody")
public class ServerConnector implements AsyncTaskResultHelper{

    private final static String USER_AGENT = "Cyanogen_update_tracker_" + BuildConfig.VERSION_NAME;
    private final static String SERVER_URL = "** Add the base URL of your API / backend here **";
    private final static String DEVICE_TYPE_URL = "check_device_types.php";
    private final static String UPDATE_TYPE_URL = "check_update_types.php";
    private final static String UPDATE_LINK_URL = "check_update_links.php";
    private List<DeviceTypeEntity> deviceTypeEntities;
    private List<UpdateTypeEntity> updateTypeEntities;
    private List<UpdateLinkEntity> updateLinkEntities;
    private boolean deviceTypesReady = false;
    private boolean updateTypesReady = false;
    private boolean updateLinksReady = false;

    public List<DeviceTypeEntity> getDeviceTypeEntities() {
        fetchDataFromServer fetchDeviceDataFromServer = new fetchDataFromServer();
        fetchDeviceDataFromServer.asyncTaskResultHelper = this;
        fetchDeviceDataFromServer.execute("device");

        while(!deviceTypesReady) {
            // We don't do anything here :)
        }

        return deviceTypeEntities;
    }

    public List<UpdateTypeEntity> getUpdateTypeEntities() {
        fetchDataFromServer fetchUpdateDataFromServer = new fetchDataFromServer();
        fetchUpdateDataFromServer.asyncTaskResultHelper = this;
        fetchUpdateDataFromServer.execute("update");
        while(!updateTypesReady) {
            // We don't do anything here :)
        }
        return updateTypeEntities;
    }

    public List<UpdateLinkEntity> getUpdateLinkEntities() {
        fetchDataFromServer fetchUpdateDataFromServer = new fetchDataFromServer();
        fetchUpdateDataFromServer.asyncTaskResultHelper = this;
        fetchUpdateDataFromServer.execute("update_link");
        while(!updateLinksReady) {
            // We don't do anything here :)
        }
        return updateLinkEntities;
    }

    private void findAllDeviceTypesFromHtmlResponse(String htmlResponse) {
        deviceTypeEntities = null;
        if(htmlResponse != null) {
            deviceTypeEntities = new ArrayList<>();
            try {
                JSONArray serverResponse = new JSONArray(htmlResponse);
                for (int i = 0; i < serverResponse.length(); i++) {
                    DeviceTypeEntity deviceTypeEntity = new DeviceTypeEntity();
                    JSONObject rawDeviceTypeEntity = serverResponse.getJSONObject(i);
                    deviceTypeEntity.setId(rawDeviceTypeEntity.getLong("id"));
                    deviceTypeEntity.setDeviceType(rawDeviceTypeEntity.getString("device_type"));
                    deviceTypeEntities.add(deviceTypeEntity);
                }
            } catch (JSONException e) {
                deviceTypeEntities = null;
                e.printStackTrace();
            }
        }
        deviceTypesReady = true;
    }


    private void findAllUpdateTypesFromHtmlResponse(String htmlResponse) {
        updateTypeEntities = null;

        if(htmlResponse != null) {
            try {
                updateTypeEntities = new ArrayList<>();
                JSONArray serverResponse = new JSONArray(htmlResponse);
                for (int i = 0; i < serverResponse.length(); i++) {
                    UpdateTypeEntity updateTypeEntity = new UpdateTypeEntity();
                    JSONObject rawUpdateTypeEntity = serverResponse.getJSONObject(i);
                    updateTypeEntity.setId(rawUpdateTypeEntity.getLong("id"));
                    updateTypeEntity.setUpdateType(rawUpdateTypeEntity.getString("update_type"));
                    updateTypeEntities.add(updateTypeEntity);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        updateTypesReady = true;
    }

    private void findAllUpdateLinksFromHtmlResponse(String htmlResponse) {
        updateLinkEntities = null;

        if(htmlResponse != null) {
            try {
                updateLinkEntities = new ArrayList<>();
                JSONArray serverResponse = new JSONArray(htmlResponse);
                for (int i = 0; i < serverResponse.length(); i++) {
                    UpdateLinkEntity updateLinkEntity = new UpdateLinkEntity();
                    JSONObject rawUpdateLinkEntity = serverResponse.getJSONObject(i);
                    updateLinkEntity.setId(rawUpdateLinkEntity.getLong("id"));
                    updateLinkEntity.setTracking_update_type_id(rawUpdateLinkEntity.getLong("tracking_update_type_id"));
                    updateLinkEntity.setTracking_device_type_id(rawUpdateLinkEntity.getLong("tracking_device_type_id"));
                    updateLinkEntity.setInformation_url(rawUpdateLinkEntity.getString("information_url"));
                    updateLinkEntities.add(updateLinkEntity);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        updateLinksReady = true;
    }

    @Override
    public void onTaskComplete(String... output) {
        if(output != null) {
            String type = output[0];
            String data = output[1];
            switch (type) {
                case "device":
                    findAllDeviceTypesFromHtmlResponse(data);
                    break;
                case "update":
                    findAllUpdateTypesFromHtmlResponse(data);
                    break;
                case "update_link":
                    findAllUpdateLinksFromHtmlResponse(data);
                    break;
            }
        }
        else {
            findAllUpdateTypesFromHtmlResponse(null);
            findAllDeviceTypesFromHtmlResponse(null);
            findAllUpdateLinksFromHtmlResponse(null);
        }

    }


    private class fetchDataFromServer extends AsyncTask<String,Void,String[]> {
        public AsyncTaskResultHelper asyncTaskResultHelper = null;

        @Override
        protected String[] doInBackground(String... types) {
            String type = types[0];

            URL requestUrl;
            try {
                switch(type) {
                    case "device":
                        requestUrl = new URL(SERVER_URL + DEVICE_TYPE_URL);
                        break;
                    case "update":
                        requestUrl = new URL(SERVER_URL + UPDATE_TYPE_URL);
                        break;
                    case "update_link":
                        requestUrl = new URL(SERVER_URL + UPDATE_LINK_URL);
                        break;
                    default:
                        deviceTypesReady=true;
                        updateTypesReady=true;
                        updateLinksReady=true;
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
                    deviceTypesReady=true;
                    updateTypesReady=true;
                    updateLinksReady=true;
                    return null;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            deviceTypesReady=true;
            updateTypesReady=true;
            updateLinksReady=true;
            return null;
        }

        @Override
        protected void onPostExecute(String[] typeAndHtmlResponse) {
            asyncTaskResultHelper.onTaskComplete(typeAndHtmlResponse);
        }

    }
}

