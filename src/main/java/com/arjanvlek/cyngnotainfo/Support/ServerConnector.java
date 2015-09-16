package com.arjanvlek.cyngnotainfo.Support;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.Model.Device;
import com.arjanvlek.cyngnotainfo.Model.ServerMessage;
import com.arjanvlek.cyngnotainfo.Model.ServerStatus;
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

public class ServerConnector {

    public final static String USER_AGENT = "Cyanogen_update_tracker_" + BuildConfig.VERSION_NAME;
    private final static String SERVER_URL = "** Add the base URL of your API / backend here **v1.1/";
    private final static String TEST_SERVER_URL = "http://cyanogenupdatetracker.com/test/api/v1.1/";
    private final static String DEVICES_URL = "devices";
    private final static String UPDATE_METHOD_URL = "updateMethods";
    private final static String UPDATE_DATA_LINK_URL = "updateDataLink";
    private final static String SERVER_STATUS_URL = "serverStatus";
    private final static String SERVER_MESSAGES_URL = "serverMessages";

    private List<Device> devices;
    private List<UpdateMethod> updateMethods;
    private UpdateDataLink updateDataLink;
    private CyanogenOTAUpdate cyanogenOTAUpdate;
    private ServerStatus serverStatus;
    private List<ServerMessage> serverMessages;

    private List<Device> offlineDevices = fillOfflineDevices();
    private List<UpdateMethod> offlineUpdateMethods = fillOfflineUpdateMethods();
    private List<UpdateDataLink> offlineUpdateDataLinks = fillOfflineUpdateDataLinks();

    private boolean offline = false;
    public static boolean testing = false;

    public List<Device> getDevices() {
        if(devices == null) {
            fetchDataFromServer("device");
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
        fetchDataFromServer("update", deviceId.toString());
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

    public ServerStatus getServerStatus() {
        fetchDataFromServer("server_status");
        return serverStatus;
    }

    public List<ServerMessage> getServerMessages() {
        fetchDataFromServer("server_messages");
        return serverMessages;
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
            else {
                offline = true;
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
            else {
                offline = true;
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
            else {
                offline = true;
            }
        } else {
            offline = true;
        }
    }

    private void findServerStatusFromHtmlResponse(String htmlResponse) {
        serverStatus = null;
        if (htmlResponse != null) {
            if(!htmlResponse.isEmpty()) {
                try {
                    serverStatus = new ServerStatus();
                    JSONArray serverResponse = new JSONArray(htmlResponse);
                    for(int i = 0; i < serverResponse.length(); i++) {
                        JSONObject rawServerStatus = serverResponse.getJSONObject(i);
                        ServerStatus.Status status;
                        switch(rawServerStatus.getString("status")) {
                            case "OK":
                                status = ServerStatus.Status.OK;
                                break;
                            case "WARNING":
                                status = ServerStatus.Status.WARNING;
                                break;
                            case "ERROR":
                                status = ServerStatus.Status.ERROR;
                                break;
                            case "TAKEN_DOWN":
                                status = ServerStatus.Status.TAKEN_DOWN;
                                break;
                            case "MAINTENANCE":
                                status = ServerStatus.Status.MAINTENANCE;
                                break;
                            default :
                                status = ServerStatus.Status.UNREACHABLE;
                        }
                        serverStatus.setStatus(status);
                        serverStatus.setLatestAppVersion(rawServerStatus.getString("latest_app_version"));
                    }
                } catch (JSONException e) {
                    serverStatus = new ServerStatus();
                    serverStatus.setStatus(ServerStatus.Status.UNREACHABLE);
                    serverStatus.setLatestAppVersion(BuildConfig.VERSION_NAME); // To prevent incorrect app update messages.
                }
            } else {
                serverStatus = new ServerStatus();
                serverStatus.setStatus(ServerStatus.Status.UNREACHABLE);
                serverStatus.setLatestAppVersion(BuildConfig.VERSION_NAME); // To prevent incorrect app update messages.
            }
        } else {
            serverStatus = new ServerStatus();
            serverStatus.setStatus(ServerStatus.Status.UNREACHABLE);
            serverStatus.setLatestAppVersion(BuildConfig.VERSION_NAME); // To prevent incorrect app update messages.
        }
    }

    private void findServerMessagesFromHtmlResponse(String htmlResponse) {
        serverMessages = null;
        if (htmlResponse != null) {
            if (!htmlResponse.isEmpty()) {
                try {
                    serverMessages = new ArrayList<>();
                    JSONArray serverResponse = new JSONArray(htmlResponse);
                    for (int i = 0; i < serverResponse.length(); i++) {
                        ServerMessage serverMessage = new ServerMessage();
                        JSONObject rawServerMessage = serverResponse.getJSONObject(i);
                        serverMessage.setId(rawServerMessage.getLong("id"));
                        serverMessage.setMessage(rawServerMessage.getString("message"));
                        serverMessage.setMessageNl(rawServerMessage.getString("message_nl"));
                        String deviceId = rawServerMessage.getString("device_id");
                        if(deviceId != null && !deviceId.equals("null")) {
                            serverMessage.setDeviceId(rawServerMessage.getLong("device_id"));
                        }
                        ServerMessage.ServerMessagePriority priority;
                        switch(rawServerMessage.getString("priority")) {
                            case "LOW":
                                priority = ServerMessage.ServerMessagePriority.LOW;
                                break;
                            case "MEDIUM":
                                priority = ServerMessage.ServerMessagePriority.MEDIUM;
                                break;
                            case "HIGH":
                                priority = ServerMessage.ServerMessagePriority.HIGH;
                                break;
                            default:
                                priority = ServerMessage.ServerMessagePriority.LOW;
                                break;
                        }
                        serverMessage.setPriority(priority);
                        switch(rawServerMessage.getString("marquee")) {
                            case "0":
                                serverMessage.setMarquee(false);
                                break;
                            case "1":
                                serverMessage.setMarquee(true);
                                break;
                            default:
                                serverMessage.setMarquee(false);
                                break;
                        }
                        serverMessages.add(serverMessage);
                    }
                } catch (JSONException e) {
                    // There should never be an exception here; it means an internal server / api error.
                    serverMessages = new ArrayList<>();
                }
            }
            else {
                serverMessages = new ArrayList<>();
            }
        } else {
            serverMessages = new ArrayList<>();
        }
    }

    private void findAllUpdateInformationFromHtmlResponse(String htmlResponse) {
        cyanogenOTAUpdate = null;

        if (htmlResponse != null) {
            if (!htmlResponse.isEmpty()) {
                try {
                    cyanogenOTAUpdate = new CyanogenOTAUpdate();
                    try {
                        JSONObject object = new JSONObject(htmlResponse);
                        object.getString("errors");
                        cyanogenOTAUpdate.setUpdateInformationAvailable(false);
                    } catch (JSONException e) {
                        cyanogenOTAUpdate.setUpdateInformationAvailable(true);
                    }
                    JSONObject object = new JSONObject(htmlResponse);
                    cyanogenOTAUpdate.setDateUpdated(object.getString("date_updated"));
                    cyanogenOTAUpdate.setSize(object.getInt("size"));
                    cyanogenOTAUpdate.setDownloadUrl(object.getString("download_url"));
                    cyanogenOTAUpdate.setFileName(object.getString("filename"));
                    cyanogenOTAUpdate.setDescription(object.getString("description"));
                    cyanogenOTAUpdate.setRollOutPercentage(object.getInt("rollout_percentage"));
                    cyanogenOTAUpdate.setName(object.getString("name"));
                    cyanogenOTAUpdate.setModel(object.getString("model"));
                    cyanogenOTAUpdate.setDateCreatedUnix(object.getInt("date_created_unix"));

                } catch (JSONException e) {
                    // No issue here: Sometimes Cyanogen decides not to include a field in the result.
                }
            }
            else {
                offline = true;
            }
        } else {
            offline = true;
        }
    }

    private void fetchDataFromServer(String requestType) {
        handleResponse(this.obtainDataFromServer(requestType, null, null));
    }

    private void fetchDataFromServer(String requestType, @Nullable String oldRegistrationToken) {
        handleResponse(this.obtainDataFromServer(requestType, oldRegistrationToken, null));
    }

    private void fetchDataFromServer(String requestType, @Nullable String oldRegistrationToken, @Nullable String cyanogenUpdateUrl) {
        handleResponse(this.obtainDataFromServer(requestType, oldRegistrationToken, cyanogenUpdateUrl));
    }


    private void handleResponse(String[] typeAndHtmlResponse) {
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
                case "server_status":
                    findServerStatusFromHtmlResponse(data);
                    break;
                case "server_messages":
                    findServerMessagesFromHtmlResponse(data);
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
    }

    private String[] obtainDataFromServer(@NonNull String requestType, @Nullable String oldRegistrationToken, @Nullable String cyanogenUpdateDataUrl) {
        offline = false;
        URL requestUrl;
        try {
            switch (requestType) {
                case "device":
                    if(!testing) {
                        requestUrl = new URL(SERVER_URL + DEVICES_URL);
                    } else{
                        requestUrl = new URL(TEST_SERVER_URL + DEVICES_URL);
                    }
                    break;
                case "update":
                    if(!testing) {
                        requestUrl = new URL(SERVER_URL + UPDATE_METHOD_URL + "/" +  oldRegistrationToken);
                    } else {
                        requestUrl = new URL(TEST_SERVER_URL + UPDATE_METHOD_URL + "/" + oldRegistrationToken);
                    }
                    break;
                case "update_link":
                    if(!testing) {
                        requestUrl = new URL(SERVER_URL + UPDATE_DATA_LINK_URL + "/" + oldRegistrationToken + "/" + cyanogenUpdateDataUrl);
                    } else {
                        requestUrl = new URL(TEST_SERVER_URL + UPDATE_DATA_LINK_URL + "/" + oldRegistrationToken + "/" + cyanogenUpdateDataUrl);
                    }
                    break;
                case "server_status":
                    if(!testing) {
                        requestUrl = new URL(SERVER_URL + SERVER_STATUS_URL);
                    } else {
                        requestUrl = new URL(TEST_SERVER_URL + SERVER_STATUS_URL);
                    }
                    break;
                case "server_messages":
                    if(!testing) {
                        requestUrl = new URL(SERVER_URL + SERVER_MESSAGES_URL);
                    } else {
                        requestUrl = new URL(TEST_SERVER_URL + SERVER_MESSAGES_URL);
                    }
                    break;
                case "cyanogen_update":
                    if(cyanogenUpdateDataUrl != null) {
                        requestUrl = new URL(cyanogenUpdateDataUrl);
                    }
                    else {
                        return null;
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
                typeAndHtmlResponse[0] = requestType;
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

    private List<Device> fillOfflineDevices() {
        List<Device> offlineDevices = new ArrayList<>();

        Device onePlusOne = new Device(1, "OnePlus One");
        Device yuYureka = new Device(2, "Yu Yureka");
        Device oppoN1 = new Device(3, "Oppo N1 Cyanogenmod Edition");
        Device yuYurekaPlus = new Device(4, "Yu Yureka Plus");
        Device yuYuphoria = new Device(5, "Yu Yuphoria");
        Device smartfrenAndromaxQ = new Device(7, "Smartfren Andromax Q");
        Device yuYunique = new Device(8, "Yu Yunique");
        Device zukZ1 = new Device(9, "ZUK Z1");
        Device wileyFoxSwift = new Device(10, "Wileyfox Swift");

        offlineDevices.add(onePlusOne);
        offlineDevices.add(yuYureka);
        offlineDevices.add(oppoN1);
        offlineDevices.add(yuYurekaPlus);
        offlineDevices.add(yuYuphoria);
        offlineDevices.add(smartfrenAndromaxQ);
        offlineDevices.add(yuYunique);
        offlineDevices.add(zukZ1);
        offlineDevices.add(wileyFoxSwift);
        return offlineDevices;
    }

    private List<UpdateMethod> fillOfflineUpdateMethods() {
        List<UpdateMethod> offlineUpdateMethods = new ArrayList<>();

        UpdateMethod fullUpdate = new UpdateMethod(1, "Full update", "Volledige update");
        UpdateMethod incrementalUpdate = new UpdateMethod(2, "Incremental update", "Incrementele update");

        offlineUpdateMethods.add(incrementalUpdate);
        offlineUpdateMethods.add(fullUpdate);
        return offlineUpdateMethods;
    }

    private List<UpdateDataLink> fillOfflineUpdateDataLinks() {
        List<UpdateDataLink> updateDataLinks = new ArrayList<>();

        // UpdateDataLink (ID, Device ID, Update Method ID, Update Data URL)
        UpdateDataLink OnePlusOneFullUpdate = new UpdateDataLink(1, 1, 2, "https://fota.cyngn.com/api/v1/update/get_latest?model=bacon&type=STABLE");
        UpdateDataLink OnePlusOneIncrementalUpdate = new UpdateDataLink(2, 1, 1, "https://fota.cyngn.com/api/v1/update/get_latest?model=bacon&type=INCREMENTAL");
        UpdateDataLink YuYurekaFullUpdate = new UpdateDataLink(3, 2, 2, "https://fota.cyngn.com/api/v1/update/get_latest?model=tomato&type=STABLE");
        UpdateDataLink YuYurekaIncrementalUpdate = new UpdateDataLink(4, 2, 1, "https://fota.cyngn.com/api/v1/update/get_latest?model=tomato&type=INCREMENTAL");
        UpdateDataLink N1FullUpdate = new UpdateDataLink(5, 3, 2, "https://fota.cyngn.com/api/v1/update/get_latest?model=n1&type=STABLE");
        UpdateDataLink N1IncrementalUpdate = new UpdateDataLink(6, 3, 1, "https://fota.cyngn.com/api/v1/update/get_latest?model=n1&type=INCREMENTAL");
        UpdateDataLink YuYurekaPlusFullUpdate = new UpdateDataLink(7, 4, 2, "https://fota.cyngn.com/api/v1/update/get_latest?model=tomato&type=STABLE");
        UpdateDataLink YuYurekaPlusIncrementalUpdate = new UpdateDataLink(8, 4, 1, "https://fota.cyngn.com/api/v1/update/get_latest?model=tomato&type=INCREMENTAL");
        UpdateDataLink YuYuphoriaFullUpdate = new UpdateDataLink(9, 5, 2, "https://fota.cyngn.com/api/v1/update/get_latest?model=lettuce&type=STABLE");
        UpdateDataLink YuYuphoriaIncrementalUpdate = new UpdateDataLink(10, 5, 1, "https://fota.cyngn.com/api/v1/update/get_latest?model=lettuce&type=INCREMENTAL");
        UpdateDataLink SmartfrenAndromaxQFullUpdate = new UpdateDataLink(11, 7, 2, "fota.cyngn.com/api/v1/update/get_latest?model=rendang&type=STABLE");
        UpdateDataLink SmartfrenAndromaxQIncrementalUpdate = new UpdateDataLink(12, 7, 1, "fota.cyngn.com/api/v1/update/get_latest?model=rendang&type=INCREMENTAL");
        UpdateDataLink YuYuniqueFullUpdate = new UpdateDataLink(13, 8, 2, "https://fota.cyngn.com/api/v1/update/get_latest?model=jalebi&type=STABLE");
        UpdateDataLink YuYuniqueIncrementalUpdate = new UpdateDataLink(14, 8, 1, "https://fota.cyngn.com/api/v1/update/get_latest?model=jalebi&type=INCREMENTAL");
        UpdateDataLink ZukZ1FullUpdate = new UpdateDataLink(15, 9, 2, "https://fota.cyngn.com/api/v1/update/get_latest?model=ham&type=STABLE");
        UpdateDataLink ZukZ1IncrementalUpdate = new UpdateDataLink(16, 9, 1, "https://fota.cyngn.com/api/v1/update/get_latest?model=ham&type=INCREMENTAL");
        UpdateDataLink WileyFoxSwiftFullUpdate = new UpdateDataLink(17, 10, 2, "https://fota.cyngn.com/api/v1/update/get_latest?model=crackling&type=STABLE");
        UpdateDataLink WileyFoxSwiftIncrementalUpdate = new UpdateDataLink(18, 10, 1, "https://fota.cyngn.com/api/v1/update/get_latest?model=crackling&type=INCREMENTAL");

        updateDataLinks.add(OnePlusOneFullUpdate);
        updateDataLinks.add(OnePlusOneIncrementalUpdate);
        updateDataLinks.add(YuYurekaFullUpdate);
        updateDataLinks.add(YuYurekaIncrementalUpdate);
        updateDataLinks.add(N1FullUpdate);
        updateDataLinks.add(N1IncrementalUpdate);
        updateDataLinks.add(YuYurekaPlusFullUpdate);
        updateDataLinks.add(YuYurekaPlusIncrementalUpdate);
        updateDataLinks.add(YuYuphoriaFullUpdate);
        updateDataLinks.add(YuYuphoriaIncrementalUpdate);
        updateDataLinks.add(SmartfrenAndromaxQFullUpdate);
        updateDataLinks.add(SmartfrenAndromaxQIncrementalUpdate);
//        updateDataLinks.add(YuYuniqueFullUpdate);
//        updateDataLinks.add(YuYuniqueIncrementalUpdate);
//        updateDataLinks.add(ZukZ1FullUpdate);
//        updateDataLinks.add(ZukZ1IncrementalUpdate);
        // For the Yu Yunique is only a FOTA test file available. This is no real update and therefore this device is disabled.
        // The ZUK Z1 does not have stable updates yet (only experimental builds), so it is explicitly not added here.
        updateDataLinks.add(WileyFoxSwiftFullUpdate);
        updateDataLinks.add(WileyFoxSwiftIncrementalUpdate);


        return updateDataLinks;

    }
}

