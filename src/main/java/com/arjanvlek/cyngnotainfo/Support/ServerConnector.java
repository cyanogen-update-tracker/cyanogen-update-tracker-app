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
    private List<DeviceTypeEntity> offlineDeviceTypeEntities = new ArrayList<>();
    private List<UpdateTypeEntity> offlineUpdateTypeEntities = new ArrayList<>();
    private List<UpdateLinkEntity> offlineUpdateLinkEntities = new ArrayList<>();
    private boolean deviceTypesReady = false;
    private boolean updateTypesReady = false;
    private boolean updateLinksReady = false;
    private boolean offline = false;

    public ServerConnector() {
        this.offlineDeviceTypeEntities = fillOfflineDeviceEntities();
        this.offlineUpdateTypeEntities = fillOfflineUpdateTypeEntities();
        this.offlineUpdateLinkEntities = fillOfflineUpdateLinkEntities();
    }

    public List<DeviceTypeEntity> getDeviceTypeEntities() {
        fetchDataFromServer fetchDeviceDataFromServer = new fetchDataFromServer();
        fetchDeviceDataFromServer.asyncTaskResultHelper = this;
        fetchDeviceDataFromServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"device");

        while(!deviceTypesReady) {
            // We don't do anything here :)
        }
        if(offline) {
            return offlineDeviceTypeEntities;
        }
        else {
            return deviceTypeEntities;
        }
    }

    public List<UpdateTypeEntity> getUpdateTypeEntities() {
        fetchDataFromServer fetchUpdateDataFromServer = new fetchDataFromServer();
        fetchUpdateDataFromServer.asyncTaskResultHelper = this;
        fetchUpdateDataFromServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "update");
        while(!updateTypesReady) {
            // We don't do anything here :)
        }
        if(offline) {
            return offlineUpdateTypeEntities;
        }
        else {
            return updateTypeEntities;
        }
    }

    public List<UpdateLinkEntity> getUpdateLinkEntities() {
        fetchDataFromServer fetchUpdateDataFromServer = new fetchDataFromServer();
        fetchUpdateDataFromServer.asyncTaskResultHelper = this;
        fetchUpdateDataFromServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "update_link");
        while(!updateLinksReady) {
            // We don't do anything here :)
        }
        if(offline) {
            return offlineUpdateLinkEntities;
        }
        else {
            return updateLinkEntities;
        }
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
        else {
            offline = true;
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
        else {
            offline = true;
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
        else {
            offline = true;
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
                    return null;
                }
            }
            catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] typeAndHtmlResponse) {
            asyncTaskResultHelper.onTaskComplete(typeAndHtmlResponse);
        }

    }

    private List<DeviceTypeEntity> fillOfflineDeviceEntities() {
        List<DeviceTypeEntity> offlineDeviceTypeEntities = new ArrayList<>();
        DeviceTypeEntity onePlusOne = new DeviceTypeEntity();
        onePlusOne.setId(1);
        onePlusOne.setDeviceType("OnePlus One");
        DeviceTypeEntity yuYureka = new DeviceTypeEntity();
        yuYureka.setId(2);
        yuYureka.setDeviceType("Yu Yureka");
        DeviceTypeEntity oppoN1 = new DeviceTypeEntity();
        oppoN1.setId(3);
        oppoN1.setDeviceType("Oppo N1 Cyanogenmod Edition");
        offlineDeviceTypeEntities.add(onePlusOne);
        offlineDeviceTypeEntities.add(yuYureka);
        offlineDeviceTypeEntities.add(oppoN1);
        return offlineDeviceTypeEntities;
    }

    private List<UpdateTypeEntity> fillOfflineUpdateTypeEntities() {
        List<UpdateTypeEntity> offlineUpdateTypeEntities = new ArrayList<>();
        UpdateTypeEntity fullUpdate = new UpdateTypeEntity();
        fullUpdate.setId(1);
        fullUpdate.setUpdateType("full_update");
        UpdateTypeEntity incrementalUpdate = new UpdateTypeEntity();
        incrementalUpdate.setId(2);
        incrementalUpdate.setUpdateType("incremental_update");
        offlineUpdateTypeEntities.add(fullUpdate);
        offlineUpdateTypeEntities.add(incrementalUpdate);
        return offlineUpdateTypeEntities;
    }

    private List<UpdateLinkEntity> fillOfflineUpdateLinkEntities() {
        List<UpdateLinkEntity> updateLinkEntities = new ArrayList<>();

        UpdateLinkEntity OnePlusOneFullUpdate = new UpdateLinkEntity();
        OnePlusOneFullUpdate.setId(1);
        OnePlusOneFullUpdate.setTracking_device_type_id(1);
        OnePlusOneFullUpdate.setTracking_update_type_id(1);
        OnePlusOneFullUpdate.setInformation_url("https://fota.cyngn.com/api/v1/update/get_latest?model=bacon&type=STABLE");

        UpdateLinkEntity OnePlusOneIncrementalUpdate = new UpdateLinkEntity();
        OnePlusOneIncrementalUpdate.setId(2);
        OnePlusOneIncrementalUpdate.setTracking_device_type_id(1);
        OnePlusOneIncrementalUpdate.setTracking_update_type_id(2);
        OnePlusOneIncrementalUpdate.setInformation_url("https://fota.cyngn.com/api/v1/update/get_latest?model=bacon&type=INCREMENTAL");

        UpdateLinkEntity YuYurekaFullUpdate = new UpdateLinkEntity();
        YuYurekaFullUpdate.setId(3);
        YuYurekaFullUpdate.setTracking_device_type_id(2);
        YuYurekaFullUpdate.setTracking_update_type_id(1);
        YuYurekaFullUpdate.setInformation_url("https://fota.cyngn.com/api/v1/update/get_latest?model=tomato&type=STABLE");

        UpdateLinkEntity YuYurekaIncrementalUpdate = new UpdateLinkEntity();
        YuYurekaIncrementalUpdate.setId(4);
        YuYurekaIncrementalUpdate.setTracking_device_type_id(2);
        YuYurekaIncrementalUpdate.setTracking_update_type_id(2);
        YuYurekaIncrementalUpdate.setInformation_url("https://fota.cyngn.com/api/v1/update/get_latest?model=tomato&type=INCREMENTAL");

        UpdateLinkEntity N1FullUpdate = new UpdateLinkEntity();
        N1FullUpdate.setId(5);
        N1FullUpdate.setTracking_device_type_id(3);
        N1FullUpdate.setTracking_update_type_id(1);
        N1FullUpdate.setInformation_url("https://fota.cyngn.com/api/v1/update/get_latest?model=n1&type=STABLE");

        UpdateLinkEntity N1IncrementalUpdate = new UpdateLinkEntity();
        N1IncrementalUpdate.setId(6);
        N1IncrementalUpdate.setTracking_device_type_id(3);
        N1IncrementalUpdate.setTracking_update_type_id(2);
        N1IncrementalUpdate.setInformation_url("https://fota.cyngn.com/api/v1/update/get_latest?model=n1&type=INCREMENTAL");

        updateLinkEntities.add(OnePlusOneFullUpdate);
        updateLinkEntities.add(OnePlusOneIncrementalUpdate);
        updateLinkEntities.add(YuYurekaFullUpdate);
        updateLinkEntities.add(YuYurekaIncrementalUpdate);
        updateLinkEntities.add(N1FullUpdate);
        updateLinkEntities.add(N1IncrementalUpdate);

        return updateLinkEntities;

    }

}

