package com.arjanvlek.cyngnotainfo.Support;

import android.support.annotation.Nullable;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.Model.Device;
import com.arjanvlek.cyngnotainfo.Model.ServerMessage;
import com.arjanvlek.cyngnotainfo.Model.ServerStatus;
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

import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.DEVICES;
import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.SERVER_MESSAGES;
import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.SERVER_STATUS;
import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.UPDATE_DATA;
import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.UPDATE_METHODS;

public class ServerConnector {

    public final static String USER_AGENT = "Cyanogen_update_tracker_" + BuildConfig.VERSION_NAME;
    public final static String SERVER_URL = "** Add the base URL of your API / backend here **v1.1/";
    public final static String TEST_SERVER_URL = "http://cyanogenupdatetracker.com/test/api/v1.1/";

    private List<Device> devices;
    private List<UpdateMethod> updateMethods;
    private CyanogenOTAUpdate cyanogenOTAUpdate;
    private ServerStatus serverStatus;
    private List<ServerMessage> serverMessages;

    public static boolean testing = false;

    public List<Device> getDevices() {
        fetchDataFromServer(DEVICES);
        return devices;
    }

    public CyanogenOTAUpdate getCyanogenOTAUpdate(Long deviceId, Long updateMethodId) {
        fetchDataFromServer(UPDATE_DATA, deviceId.toString(), updateMethodId.toString());
        return cyanogenOTAUpdate;
    }

    public List<UpdateMethod> getUpdateMethods(Long deviceId) {
        fetchDataFromServer(UPDATE_METHODS, deviceId.toString());
        return updateMethods;
    }

    public ServerStatus getServerStatus() {
        fetchDataFromServer(SERVER_STATUS);
        return serverStatus;
    }

    public List<ServerMessage> getServerMessages() {
        fetchDataFromServer(SERVER_MESSAGES);
        return serverMessages;
    }

    private void findAllDevicesFromResponse(String response) {
        devices = null;
        if (!response.isEmpty()) {
            devices = new ArrayList<>();
            try {
                JSONArray serverResponse = new JSONArray(response);
                for (int i = 0; i < serverResponse.length(); i++) {
                    Device device = new Device();
                    JSONObject rawDevice = serverResponse.getJSONObject(i);
                    device.setId(rawDevice.getLong("id"));
                    device.setDeviceName(rawDevice.getString("device_name"));
                    devices.add(device);
                }
            } catch (JSONException e) {
                // There should never be an exception here; it means an internal server / api error.
            }
        }
    }

    private void findAllUpdateMethodsFromResponse(String response) {
        updateMethods = null;
        if (!response.isEmpty()) {
            try {
                updateMethods = new ArrayList<>();
                JSONArray serverResponse = new JSONArray(response);
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
            }
        }
    }

    private void findServerStatusFromResponse(String response) {
        serverStatus = null;
        if(!response.isEmpty()) {
            try {
                serverStatus = new ServerStatus();
                JSONArray serverResponse = new JSONArray(response);
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
    }

    private void findServerMessagesFromResponse(String response) {
        serverMessages = null;
        if (!response.isEmpty()) {
            try {
                serverMessages = new ArrayList<>();
                JSONArray serverResponse = new JSONArray(response);
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
    }

    private void findAllUpdateInformationFromResponse(String response) {
        cyanogenOTAUpdate = null;

        if (!response.isEmpty()) {
            try {
                cyanogenOTAUpdate = new CyanogenOTAUpdate();
                try {
                    JSONObject object = new JSONObject(response);
                    object.getString("errors");
                    cyanogenOTAUpdate.setUpdateInformationAvailable(false);
                } catch (JSONException e) {
                    cyanogenOTAUpdate.setUpdateInformationAvailable(true);
                }
                JSONObject object = new JSONObject(response);
                cyanogenOTAUpdate.setDateUpdated(object.getString("date_updated"));
                cyanogenOTAUpdate.setSize(object.getInt("size"));
                cyanogenOTAUpdate.setMD5Sum(object.getString("md5sum"));
                cyanogenOTAUpdate.setDownloadUrl(object.getString("download_url"));
                cyanogenOTAUpdate.setFileName(object.getString("filename"));
                cyanogenOTAUpdate.setDescription(object.getString("description"));
                cyanogenOTAUpdate.setRollOutPercentage(object.getInt("rollout_percentage"));
                cyanogenOTAUpdate.setName(object.getString("name"));

            } catch (JSONException e) {
                // No issue here: Sometimes Cyanogen decides not to include a field in the result.
            }
        }
    }

    private void fetchDataFromServer(ServerRequest request) {
        handleResponse(this.obtainDataFromServer(request));
    }

    private void fetchDataFromServer(ServerRequest request, @Nullable String... params) {
        handleResponse(this.obtainDataFromServer(request, params));
    }


    private void handleResponse(Object[] typeAndResponse) {
        ServerRequest type = (ServerRequest) typeAndResponse[0];
        String data = (String) typeAndResponse[1];
        switch (type) {
            case DEVICES:
                findAllDevicesFromResponse(data);
                break;
            case UPDATE_METHODS:
                findAllUpdateMethodsFromResponse(data);
                break;
            case UPDATE_DATA:
                findAllUpdateInformationFromResponse(data);
                break;
            case SERVER_STATUS:
                findServerStatusFromResponse(data);
                break;
            case SERVER_MESSAGES:
                findServerMessagesFromResponse(data);
                break;
        }
    }

    private Object[] obtainDataFromServer(ServerRequest request, String... params) {
        Object[] typeAndResponse = new Object[2];
        typeAndResponse[0] = request;
        typeAndResponse[1] = "";
        try {
            URL requestUrl = request.getURL(params);

            HttpURLConnection urlConnection = (HttpURLConnection) requestUrl.openConnection();

            //setup request
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);

            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            typeAndResponse[1] = response.toString();
            in.close();
            return typeAndResponse;
        } catch (Exception e) {
            return typeAndResponse;
        }
    }
}

