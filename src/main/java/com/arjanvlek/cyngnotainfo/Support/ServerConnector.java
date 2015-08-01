package com.arjanvlek.cyngnotainfo.Support;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.Model.Device;
import com.arjanvlek.cyngnotainfo.Model.UpdateDataLink;
import com.arjanvlek.cyngnotainfo.Model.UpdateMethod;

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
public class ServerConnector {

    public final static String USER_AGENT = "Cyanogen_update_tracker_" + BuildConfig.VERSION_NAME;
    private final static String SERVER_URL = "** Add the base URL of your API / backend here **v1/";
    private final static String TEST_SERVER_URL = "http://cyanogenupdatetracker.com/test/api/v1/";
    private final static String DEVICES_URL = "devices";
    private final static String UPDATE_METHOD_URL = "updateMethods";
    private final static String UPDATE_DATA_LINK_URL = "updateDataLink";

    private List<Device> devices;
    private List<UpdateMethod> updateMethods;
    private UpdateDataLink updateDataLink;
    private CyanogenOTAUpdate cyanogenOTAUpdate;

    private List<Device> offlineDevices = fillOfflineDevices();
    private List<UpdateMethod> offlineUpdateMethods = fillOfflineUpdateMethods();
    private List<UpdateDataLink> offlineUpdateDataLinks = fillOfflineUpdateDataLinks();

    private boolean offline = false;
    private boolean ready = false;
    public static boolean testing = false;

    public List<Device> getDevices() {
        if(devices == null) {
            fetchDataFromServer("device", null, null);
            if (offline) {
                return offlineDevices;
            } else {
                return devices;
            }
        }
        else {
            return devices;
        }
    }

    public CyanogenOTAUpdate getCyanogenOTAUpdate(String updateInformationUrl) {
        fetchDataFromServer("cyanogen_update", null, updateInformationUrl);
        if (offline) {
            return null;
        } else {
            return cyanogenOTAUpdate;
        }
    }

    public List<UpdateMethod> getUpdateMethods(Long deviceId) {
        fetchDataFromServer("update", deviceId.toString(), null);
        if (offline) {
            return offlineUpdateMethods;
        } else {
            return updateMethods;
        }
    }

    public UpdateDataLink getUpdateDataLink(Long deviceId, Long updateMethodId) {
        fetchDataFromServer("update_link", deviceId.toString(), updateMethodId.toString());
        if (offline) {
            UpdateDataLink offlineUpdateDataLink = null;
            for(UpdateDataLink updateDataLink : offlineUpdateDataLinks) {
                if(updateDataLink.getDeviceId() == deviceId && updateDataLink.getUpdateMethodId() == updateMethodId) {
                    offlineUpdateDataLink = updateDataLink;
                }
            }
            return offlineUpdateDataLink;
        } else {
            return updateDataLink;
        }
    }

    private void findAllDevicesFromHtmlResponse(String htmlResponse) {
        devices = null;
        if (htmlResponse != null) {
            if (!htmlResponse.isEmpty()) {
                devices = new ArrayList<>();
                try {
                    JSONArray serverResponse = new JSONArray(htmlResponse);
                    for (int i = 0; i < serverResponse.length(); i++) {
                        Device device = new Device();
                        JSONObject rawDevice = serverResponse.getJSONObject(i);
                        device.setId(rawDevice.getLong("id"));
                        device.setDeviceName(rawDevice.getString("device_name"));
                        devices.add(device);
                    }
                } catch (JSONException e) {
                    // There should never be an exception here; it means an internal server / api error.
                    offline = true;
                }
            }
        } else {
            offline = true;
        }
    }

    private void findAllUpdateMethodsFromHtmlResponse(String htmlResponse) {
        updateMethods = null;

        if (htmlResponse != null) {
            if (!htmlResponse.isEmpty()) {
                try {
                    updateMethods = new ArrayList<>();
                    JSONArray serverResponse = new JSONArray(htmlResponse);
                    for (int i = 0; i < serverResponse.length(); i++) {
                        UpdateMethod updateMethod = new UpdateMethod();
                        JSONObject rawUpdateMethod = serverResponse.getJSONObject(i);
                        updateMethod.setId(rawUpdateMethod.getLong("id"));
                        updateMethod.setUpdateMethod(rawUpdateMethod.getString("update_method"));
                        updateMethod.setUpdateMethodNl(rawUpdateMethod.getString("update_method_nl"));
                        updateMethods.add(updateMethod);
                    }
                } catch (JSONException e) {
                    // There should never be an exception here; it means an internal server / api error.
                    offline = true;
                }
            }
        } else {
            offline = true;
        }
    }

    private void findUpdateDataLinkFromHtmlResponse(String htmlResponse) {
        updateDataLink = null;

        if (htmlResponse != null) {
            if (!htmlResponse.isEmpty()) {
                try {
                    updateDataLink = new UpdateDataLink();
                    JSONArray serverResponse = new JSONArray(htmlResponse);
                    for (int i = 0; i < serverResponse.length(); i++) {
                        JSONObject rawUpdateDataLink = serverResponse.getJSONObject(i);
                        updateDataLink.setId(rawUpdateDataLink.getLong("id"));
                        updateDataLink.setUpdateMethodId(rawUpdateDataLink.getLong("update_method_id"));
                        updateDataLink.setDeviceId(rawUpdateDataLink.getLong("device_id"));
                        updateDataLink.setUpdateDataUrl(rawUpdateDataLink.getString("update_data_url"));
                    }
                } catch (JSONException e) {
                    // There should never be an exception here; it means an internal server / api error.
                    offline = true;
                }
            }
        } else {
            offline = true;
        }
    }

    private void findAllUpdateInformationFromHtmlResponse(String htmlResponse) {
        cyanogenOTAUpdate = null;

        if (htmlResponse != null) {
            if (!htmlResponse.isEmpty()) {
                try {
                    cyanogenOTAUpdate = new CyanogenOTAUpdate();
                    JSONObject object = new JSONObject(htmlResponse);
                    cyanogenOTAUpdate.setDateUpdated(object.getString("date_updated"));
                    cyanogenOTAUpdate.setSize(object.getInt("size"));
                    cyanogenOTAUpdate.setDownloadUrl(object.getString("download_url"));
                    cyanogenOTAUpdate.setFileName(object.getString("filename"));
                    cyanogenOTAUpdate.setDescription(object.getString("description"));
                    cyanogenOTAUpdate.setRollOutPercentage(object.getInt("rollout_percentage"));
                    cyanogenOTAUpdate.setName(object.getString("name"));
                    cyanogenOTAUpdate.setModel(object.getString("model"));
                } catch (JSONException e) {
                    // No issue here: Sometimes Cyanogen decides not to include a field in the result.
                }
            }
        } else {
            offline = true;
        }
    }
    private synchronized void fetchDataFromServer(String requestType, @Nullable String oldRegistrationToken, @Nullable String cyanogenUpdateDataUrl) {
        ready = false;
        offline = false;
        FetchDataFromServer fetchDataFromServer = new FetchDataFromServer();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            fetchDataFromServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestType, oldRegistrationToken, cyanogenUpdateDataUrl);
        } else {
            fetchDataFromServer.execute(requestType, oldRegistrationToken, cyanogenUpdateDataUrl);
        }
        while(!ready) {
            // we don't do anything here while the server is working!
        }
    }

    /**
     * Fetches data from the App Server.
     * @params String array with data type at position zero and optional device id at position 1 (for update information).
     */
    private class FetchDataFromServer extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            String type = params[0];
            String deviceId;
            String updateMethodId;
            URL requestUrl;
            try {
                switch (type) {
                    case "device":
                        if(!testing) {
                            requestUrl = new URL(SERVER_URL + DEVICES_URL);
                        } else{
                            requestUrl = new URL(TEST_SERVER_URL + DEVICES_URL);
                        }
                        break;
                    case "update":
                        deviceId = params[1];
                        if(!testing) {
                            requestUrl = new URL(SERVER_URL + UPDATE_METHOD_URL + "/" +  deviceId);
                        }
                        else {
                            requestUrl = new URL(TEST_SERVER_URL + UPDATE_METHOD_URL + "/" + deviceId);
                        }
                        break;
                    case "update_link":
                        deviceId = params[1];
                        updateMethodId = params[2];
                        if(!testing) {
                            requestUrl = new URL(SERVER_URL + UPDATE_DATA_LINK_URL + "/" + deviceId + "/" + updateMethodId);
                        } else {
                            requestUrl = new URL(TEST_SERVER_URL + UPDATE_DATA_LINK_URL + "/" + deviceId + "/" + updateMethodId);
                        }
                        break;
                    case "cyanogen_update":
                        String updateDataUrl = params[2];
                        requestUrl = new URL(updateDataUrl);
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
            if (typeAndHtmlResponse != null) {
                String type = typeAndHtmlResponse[0];
                String data = typeAndHtmlResponse[1];
                switch (type) {
                    case "device":
                        findAllDevicesFromHtmlResponse(data);
                        break;
                    case "update":
                        findAllUpdateMethodsFromHtmlResponse(data);
                        break;
                    case "update_link":
                        findUpdateDataLinkFromHtmlResponse(data);
                        break;
                    case "cyanogen_update":
                        findAllUpdateInformationFromHtmlResponse(data);
                }
            } else {
                findAllUpdateMethodsFromHtmlResponse(null);
                findAllDevicesFromHtmlResponse(null);
                findUpdateDataLinkFromHtmlResponse(null);
                findAllUpdateInformationFromHtmlResponse(null);
                findAllUpdateInformationFromHtmlResponse(null);
            }
            ready = true;
        }

    }

    private List<Device> fillOfflineDevices() {
        List<Device> offlineDevices = new ArrayList<>();
        Device onePlusOne = new Device();
        onePlusOne.setId(1);
        onePlusOne.setDeviceName("OnePlus One");
        Device yuYureka = new Device();
        yuYureka.setId(2);
        yuYureka.setDeviceName("Yu Yureka");
        Device oppoN1 = new Device();
        oppoN1.setId(3);
        oppoN1.setDeviceName("Oppo N1 Cyanogenmod Edition");
        Device yuYuphoria = new Device();
        yuYuphoria.setId(5); // The ID 4 is reserved for the Alcatel Onetouch Hero 2+
        yuYuphoria.setDeviceName("Yu Yuphoria");

        offlineDevices.add(onePlusOne);
        offlineDevices.add(yuYureka);
        offlineDevices.add(oppoN1);
        offlineDevices.add(yuYuphoria);
        return offlineDevices;
    }

    private List<UpdateMethod> fillOfflineUpdateMethods() {
        List<UpdateMethod> offlineUpdateMethods = new ArrayList<>();
        UpdateMethod fullUpdate = new UpdateMethod();
        fullUpdate.setId(1);
        fullUpdate.setUpdateMethod("Full update");
        fullUpdate.setUpdateMethodNl("Volledige update");
        UpdateMethod incrementalUpdate = new UpdateMethod();
        incrementalUpdate.setId(2);
        incrementalUpdate.setUpdateMethod("Incremental update");
        incrementalUpdate.setUpdateMethodNl("Incrementele update");

        offlineUpdateMethods.add(incrementalUpdate);
        offlineUpdateMethods.add(fullUpdate);
        return offlineUpdateMethods;
    }

    private List<UpdateDataLink> fillOfflineUpdateDataLinks() {
        List<UpdateDataLink> updateDataLinks = new ArrayList<>();

        UpdateDataLink OnePlusOneFullUpdate = new UpdateDataLink();
        OnePlusOneFullUpdate.setId(1);
        OnePlusOneFullUpdate.setDeviceId(1);
        OnePlusOneFullUpdate.setUpdateMethodId(2);
        OnePlusOneFullUpdate.setUpdateDataUrl("https://fota.cyngn.com/api/v1/update/get_latest?model=bacon&type=STABLE");

        UpdateDataLink OnePlusOneIncrementalUpdate = new UpdateDataLink();
        OnePlusOneIncrementalUpdate.setId(2);
        OnePlusOneIncrementalUpdate.setDeviceId(1);
        OnePlusOneIncrementalUpdate.setUpdateMethodId(1);
        OnePlusOneIncrementalUpdate.setUpdateDataUrl("https://fota.cyngn.com/api/v1/update/get_latest?model=bacon&type=INCREMENTAL");

        UpdateDataLink YuYurekaFullUpdate = new UpdateDataLink();
        YuYurekaFullUpdate.setId(3);
        YuYurekaFullUpdate.setDeviceId(2);
        YuYurekaFullUpdate.setUpdateMethodId(2);
        YuYurekaFullUpdate.setUpdateDataUrl("https://fota.cyngn.com/api/v1/update/get_latest?model=tomato&type=STABLE");

        UpdateDataLink YuYurekaIncrementalUpdate = new UpdateDataLink();
        YuYurekaIncrementalUpdate.setId(4);
        YuYurekaIncrementalUpdate.setDeviceId(2);
        YuYurekaIncrementalUpdate.setUpdateMethodId(1);
        YuYurekaIncrementalUpdate.setUpdateDataUrl("https://fota.cyngn.com/api/v1/update/get_latest?model=tomato&type=INCREMENTAL");

        UpdateDataLink N1FullUpdate = new UpdateDataLink();
        N1FullUpdate.setId(5);
        N1FullUpdate.setDeviceId(3);
        N1FullUpdate.setUpdateMethodId(2);
        N1FullUpdate.setUpdateDataUrl("https://fota.cyngn.com/api/v1/update/get_latest?model=n1&type=STABLE");

        UpdateDataLink N1IncrementalUpdate = new UpdateDataLink();
        N1IncrementalUpdate.setId(6);
        N1IncrementalUpdate.setDeviceId(3);
        N1IncrementalUpdate.setUpdateMethodId(1);
        N1IncrementalUpdate.setUpdateDataUrl("https://fota.cyngn.com/api/v1/update/get_latest?model=n1&type=INCREMENTAL");

        UpdateDataLink YuYuphoriaFullUpdate = new UpdateDataLink();
        YuYuphoriaFullUpdate.setId(9); //The IDs 7 and 8 are reserved for the Alcatel Onetouch Hero 2+
        YuYuphoriaFullUpdate.setDeviceId(5);
        YuYuphoriaFullUpdate.setUpdateMethodId(2);
        YuYuphoriaFullUpdate.setUpdateDataUrl("https://fota.cyngn.com/api/v1/update/get_latest?model=lettuce&type=STABLE");

        UpdateDataLink YuYuphoriaIncrementalUpdate = new UpdateDataLink();
        YuYuphoriaIncrementalUpdate.setId(10);
        YuYuphoriaIncrementalUpdate.setDeviceId(5);
        YuYuphoriaIncrementalUpdate.setUpdateMethodId(1);
        YuYuphoriaIncrementalUpdate.setUpdateDataUrl("https://fota.cyngn.com/api/v1/update/get_latest?model=lettuce&type=INCREMENTAL");

        updateDataLinks.add(OnePlusOneFullUpdate);
        updateDataLinks.add(OnePlusOneIncrementalUpdate);
        updateDataLinks.add(YuYurekaFullUpdate);
        updateDataLinks.add(YuYurekaIncrementalUpdate);
        updateDataLinks.add(N1FullUpdate);
        updateDataLinks.add(N1IncrementalUpdate);
        updateDataLinks.add(YuYuphoriaFullUpdate);
        updateDataLinks.add(YuYuphoriaIncrementalUpdate);

        return updateDataLinks;

    }
}

