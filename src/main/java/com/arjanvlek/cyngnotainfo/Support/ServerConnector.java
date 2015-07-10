package com.arjanvlek.cyngnotainfo.Support;

import android.os.AsyncTask;
import android.os.Build;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
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
public class ServerConnector implements AsyncTaskResultHelper {

    public final static String USER_AGENT = "Cyanogen_update_tracker_" + BuildConfig.VERSION_NAME;
    private final static String SERVER_URL = "** Add the base URL of your API / backend here **";
    private final static String TEST_SERVER_URL = "http://192.168.178.14/";
    private final static String DEVICE_TYPE_URL = "check_device_types.php";
    private final static String UPDATE_TYPE_URL = "check_update_types.php";
    private final static String UPDATE_LINK_URL = "check_update_links.php";
    private List<DeviceTypeEntity> deviceTypeEntities;
    private List<UpdateTypeEntity> updateTypeEntities;
    private List<UpdateLinkEntity> updateLinkEntities;
    private CyanogenOTAUpdate cyanogenOTAUpdate;
    private List<DeviceTypeEntity> offlineDeviceTypeEntities = new ArrayList<>();
    private List<UpdateTypeEntity> offlineUpdateTypeEntities = new ArrayList<>();
    private List<UpdateLinkEntity> offlineUpdateLinkEntities = new ArrayList<>();
    private boolean deviceTypesReady = false;
    private boolean updateTypesReady = false;
    private boolean updateLinksReady = false;
    private boolean cyanogenUpdateReady = false;
    private boolean offline = false;
    private boolean testing = false;

    public ServerConnector() {
        this.offlineDeviceTypeEntities = fillOfflineDeviceEntities();
        this.offlineUpdateTypeEntities = fillOfflineUpdateTypeEntities();
        this.offlineUpdateLinkEntities = fillOfflineUpdateLinkEntities();
        this.testing = false;
    }

    public ServerConnector(boolean isTesting) {
        this.offlineDeviceTypeEntities = fillOfflineDeviceEntities();
        this.offlineUpdateTypeEntities = fillOfflineUpdateTypeEntities();
        this.offlineUpdateLinkEntities = fillOfflineUpdateLinkEntities();
        this.testing = isTesting;
    }

    public List<DeviceTypeEntity> getDeviceTypeEntities() {
        fetchDataFromServer fetchDeviceDataFromServer = new fetchDataFromServer();
        fetchDeviceDataFromServer.asyncTaskResultHelper = this;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            fetchDeviceDataFromServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "device");
        } else {
            fetchDeviceDataFromServer.execute("device");
        }

        while (!deviceTypesReady) {
            // We don't do anything here :)
        }
        if (offline) {
            return offlineDeviceTypeEntities;
        } else {
            return deviceTypeEntities;
        }
    }



    public CyanogenOTAUpdate fetchCyanogenOtaUpdate(String updateInformationUrl) {
        fetchDataFromCyanogenServer fetchDataFromCyanogenServer = new fetchDataFromCyanogenServer();
        fetchDataFromCyanogenServer.asyncTaskResultHelper = this;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            fetchDataFromCyanogenServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, updateInformationUrl);
        } else {
            fetchDataFromCyanogenServer.execute(updateInformationUrl);
        }

        while (!cyanogenUpdateReady) {
            // We don't do anything here :)
        }
        if (offline) {
            return null;
        } else {
            return cyanogenOTAUpdate;
        }

    }

    public List<UpdateTypeEntity> getUpdateTypeEntities(String deviceId) {
        fetchDataFromServer fetchUpdateDataFromServer = new fetchDataFromServer();
        fetchUpdateDataFromServer.asyncTaskResultHelper = this;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            fetchUpdateDataFromServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "update", deviceId);
        } else {
            fetchUpdateDataFromServer.execute("update", deviceId);
        }
        while (!updateTypesReady) {
            // We don't do anything here :)
        }
        if (offline) {
            return offlineUpdateTypeEntities;
        } else {
            return updateTypeEntities;
        }
    }

    public List<UpdateLinkEntity> getUpdateLinkEntities() {
        fetchDataFromServer fetchUpdateLinksFromServer = new fetchDataFromServer();
        fetchUpdateLinksFromServer.asyncTaskResultHelper = this;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            fetchUpdateLinksFromServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "update_link");
        } else {
            fetchUpdateLinksFromServer.execute("update_link");
        }
        while (!updateLinksReady) {
            // We don't do anything here :)
        }
        if (offline) {
            return offlineUpdateLinkEntities;
        } else {
            return updateLinkEntities;
        }
    }

    private void findAllDeviceTypesFromHtmlResponse(String htmlResponse) {
        deviceTypeEntities = null;
        if (htmlResponse != null) {
            if (!htmlResponse.isEmpty()) {
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
        } else {
            offline = true;
        }
        deviceTypesReady = true;
    }


    private void findAllUpdateTypesFromHtmlResponse(String htmlResponse) {
        updateTypeEntities = null;

        if (htmlResponse != null) {
            if (!htmlResponse.isEmpty()) {
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
        } else {
            offline = true;
        }
        updateTypesReady = true;
    }

    private void findAllUpdateLinksFromHtmlResponse(String htmlResponse) {
        updateLinkEntities = null;

        if (htmlResponse != null) {
            if (!htmlResponse.isEmpty()) {
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
        } else {
            offline = true;
        }
        updateLinksReady = true;
    }

    private void findAllUpdateInformationFromHtmlResponse(String htmlResponse) {
        cyanogenOTAUpdate = null;

        if (htmlResponse != null) {
            if (!htmlResponse.isEmpty()) {
                try {
                    cyanogenOTAUpdate = new CyanogenOTAUpdate();
                    JSONObject object = new JSONObject(htmlResponse);
                    cyanogenOTAUpdate.setDateUpdated(object.getString("date_updated"));
                    cyanogenOTAUpdate.setIncremental(object.getString("incremental"));
                    cyanogenOTAUpdate.setRequiredIncremental(object.getBoolean("required_incremental"));
                    cyanogenOTAUpdate.setSize(object.getInt("size"));
                    cyanogenOTAUpdate.setBuildNumber(object.getString("build_number"));
                    cyanogenOTAUpdate.setIncrementalParent(object.getString("incremental_parent"));
                    cyanogenOTAUpdate.setDownloadUrl(object.getString("download_url"));
                    cyanogenOTAUpdate.setFileName(object.getString("filename"));
                    cyanogenOTAUpdate.setSha1Sum(object.getString("sha1sum"));
                    cyanogenOTAUpdate.setType(object.getString("type"));
                    cyanogenOTAUpdate.setDescription(object.getString("description"));
                    cyanogenOTAUpdate.setDateCreatedUnix(object.getString("date_created_unix"));
                    cyanogenOTAUpdate.setRollOutPercentage(object.getInt("rollout_percentage"));
                    cyanogenOTAUpdate.setKey(object.getString("key"));
                    cyanogenOTAUpdate.setPath(object.getString("path"));
                    cyanogenOTAUpdate.setName(object.getString("name"));
                    cyanogenOTAUpdate.setMd5Sum(object.getString("md5sum"));
                    cyanogenOTAUpdate.setPublished(object.getBoolean("published"));
                    cyanogenOTAUpdate.setDateCreated(object.getString("date_created"));
                    cyanogenOTAUpdate.setModel(object.getString("model"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            offline = true;
        }
        cyanogenUpdateReady = true;
    }

    @Override
    public void onTaskComplete(String... output) {
        if (output != null) {
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
                case "information":
                    findAllUpdateInformationFromHtmlResponse(data);
                    break;
            }
        } else {
            findAllUpdateTypesFromHtmlResponse(null);
            findAllDeviceTypesFromHtmlResponse(null);
            findAllUpdateLinksFromHtmlResponse(null);
            findAllUpdateInformationFromHtmlResponse(null);
        }

    }


    /**
     * Fetches data from the App Server.
     * @params String array with data type at position zero and optional device id at position 1 (for update information).
     */
    private class fetchDataFromServer extends AsyncTask<String, Void, String[]> {
        public AsyncTaskResultHelper asyncTaskResultHelper = null;

        @Override
        protected String[] doInBackground(String... types) {
            String type = types[0];

            URL requestUrl;
            try {
                switch (type) {
                    case "device":
                        if(!testing) {
                            requestUrl = new URL(SERVER_URL + DEVICE_TYPE_URL);
                        } else{
                            requestUrl = new URL(TEST_SERVER_URL + DEVICE_TYPE_URL);
                        }
                        break;
                    case "update":
                        String deviceId = types[1];
                        if(!testing) {
                            requestUrl = new URL(SERVER_URL + UPDATE_TYPE_URL + "?device_id=" + deviceId);
                        }
                        else {
                            requestUrl = new URL(TEST_SERVER_URL + UPDATE_TYPE_URL + "?device_id=" + deviceId);
                        }
                        break;
                    case "update_link":
                        if(!testing) {
                            requestUrl = new URL(SERVER_URL + UPDATE_LINK_URL);
                        } else {
                            requestUrl = new URL(TEST_SERVER_URL + UPDATE_LINK_URL);
                        }
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
                if (responseCode >= 200 && responseCode <= 300) {
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
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] typeAndHtmlResponse) {
            asyncTaskResultHelper.onTaskComplete(typeAndHtmlResponse);
        }

    }

    private class fetchDataFromCyanogenServer extends AsyncTask<String, Void, String[]> {
        public AsyncTaskResultHelper asyncTaskResultHelper = null;

        @Override
        protected String[] doInBackground(String... updateInformationUrls) {
            String url = updateInformationUrls[0];

            URL requestUrl;
            try {
                requestUrl = new URL(url);

                HttpURLConnection urlConnection = (HttpURLConnection) requestUrl.openConnection();

                //setup request
                urlConnection.setRequestProperty("User-Agent", USER_AGENT);
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);

                int responseCode = urlConnection.getResponseCode();
                if (responseCode >= 200 && responseCode <= 300) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    String[] typeAndHtmlResponse = new String[2];
                    typeAndHtmlResponse[0] = "information";
                    typeAndHtmlResponse[1] = response.toString();
                    in.close();
                    return typeAndHtmlResponse;


                } else {
                    return null;
                }
            } catch (Exception e) {
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
        DeviceTypeEntity yuYuphoria = new DeviceTypeEntity();
        yuYuphoria.setId(5); // The ID 4 is reserved for the Alcatel Onetouch Hero 2+
        yuYuphoria.setDeviceType("Yu Yuphoria");

        offlineDeviceTypeEntities.add(onePlusOne);
        offlineDeviceTypeEntities.add(yuYureka);
        offlineDeviceTypeEntities.add(oppoN1);
        offlineDeviceTypeEntities.add(yuYuphoria);
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

        UpdateLinkEntity YuYuphoriaFullUpdate = new UpdateLinkEntity();
        YuYuphoriaFullUpdate.setId(9); //The IDs 7 and 8 are reserved for the Alcatel Onetouch Hero 2+
        YuYuphoriaFullUpdate.setTracking_device_type_id(5);
        YuYuphoriaFullUpdate.setTracking_update_type_id(1);
        YuYuphoriaFullUpdate.setInformation_url("https://fota.cyngn.com/api/v1/update/get_latest?model=lettuce&type=STABLE");

        UpdateLinkEntity YuYuphoriaIncrementalUpdate = new UpdateLinkEntity();
        YuYuphoriaIncrementalUpdate.setId(10);
        YuYuphoriaIncrementalUpdate.setTracking_device_type_id(5);
        YuYuphoriaIncrementalUpdate.setTracking_update_type_id(2);
        YuYuphoriaIncrementalUpdate.setInformation_url("https://fota.cyngn.com/api/v1/update/get_latest?model=lettuce&type=INCREMENTAL");

        updateLinkEntities.add(OnePlusOneFullUpdate);
        updateLinkEntities.add(OnePlusOneIncrementalUpdate);
        updateLinkEntities.add(YuYurekaFullUpdate);
        updateLinkEntities.add(YuYurekaIncrementalUpdate);
        updateLinkEntities.add(N1FullUpdate);
        updateLinkEntities.add(N1IncrementalUpdate);
        updateLinkEntities.add(YuYuphoriaFullUpdate);
        updateLinkEntities.add(YuYuphoriaIncrementalUpdate);

        return updateLinkEntities;

    }

}

