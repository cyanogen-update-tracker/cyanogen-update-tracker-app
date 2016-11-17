package com.arjanvlek.cyngnotainfo.Support;

import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.Model.Device;
import com.arjanvlek.cyngnotainfo.Model.InstallGuideData;
import com.arjanvlek.cyngnotainfo.Model.ServerMessage;
import com.arjanvlek.cyngnotainfo.Model.ServerStatus;
import com.arjanvlek.cyngnotainfo.Model.UpdateMethod;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.arjanvlek.cyngnotainfo.ApplicationContext.APP_USER_AGENT;
import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.DEVICES;
import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.INSTALL_GUIDE;
import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.MOST_RECENT_UPDATE_DATA;
import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.SERVER_MESSAGES;
import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.SERVER_STATUS;
import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.UPDATE_DATA;
import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.UPDATE_METHODS;

public class ServerConnector {

    final static String SERVER_URL = "** Add the base URL of your API / backend here **";
    final static String TEST_SERVER_URL = "** Add the base URL of your test API / backend here **";

    private ObjectMapper objectMapper;

    public ServerConnector() {
        this.objectMapper = new ObjectMapper();
    }

    public List<Device> getDevices() {
        return findMultipleFromServerResponse(fetchDataFromServer(DEVICES, 30), Device.class);
    }

    public CyanogenOTAUpdate getCyanogenOTAUpdate(Long deviceId, Long updateMethodId, String incrementalSystemVersion) {
        return findOneFromServerResponse(fetchDataFromServer(UPDATE_DATA, 30, deviceId.toString(), updateMethodId.toString(), incrementalSystemVersion), CyanogenOTAUpdate.class);
    }

    public CyanogenOTAUpdate getMostRecentCyanogenOTAUpdate(Long deviceId, Long updateMethodId) {
        return findOneFromServerResponse(fetchDataFromServer(MOST_RECENT_UPDATE_DATA, 30, deviceId.toString(), updateMethodId.toString()), CyanogenOTAUpdate.class);
    }

    public List<UpdateMethod> getUpdateMethods(Long deviceId) {
        return findMultipleFromServerResponse(fetchDataFromServer(UPDATE_METHODS, 30, deviceId.toString()), UpdateMethod.class);
    }

    public ServerStatus getServerStatus() {
        return findOneFromServerResponse(fetchDataFromServer(SERVER_STATUS, 30), ServerStatus.class);
    }

    public List<ServerMessage> getServerMessages(Long deviceId, Long updateMethodId) {
        return findMultipleFromServerResponse(fetchDataFromServer(SERVER_MESSAGES, 30, deviceId.toString(), updateMethodId.toString()), ServerMessage.class);
    }

    public InstallGuideData fetchInstallGuidePageFromServer(Long deviceId, Long updateMethodId, Integer pageNumber) {
        return findOneFromServerResponse(fetchDataFromServer(INSTALL_GUIDE, 10, deviceId.toString(), updateMethodId.toString(), pageNumber.toString()), InstallGuideData.class);
    }

    private <T> List<T> findMultipleFromServerResponse(String response, Class<T> returnClass) {
        try {
            return objectMapper.readValue(response, objectMapper.getTypeFactory().constructCollectionType(List.class, returnClass));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private <T> T findOneFromServerResponse(String response, Class<T> returnClass) {
        try {
            return objectMapper.readValue(response, returnClass);
        } catch(Exception e) {
            return null;
        }
    }

    private String fetchDataFromServer(ServerRequest request, int timeout, String... params) {

        try {
            URL requestUrl = request.getURL(params);

            HttpURLConnection urlConnection = (HttpURLConnection) requestUrl.openConnection();

            //setup request
            urlConnection.setRequestProperty("User-Agent", APP_USER_AGENT);
            urlConnection.setConnectTimeout(timeout);
            urlConnection.setReadTimeout(timeout);

            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }
}

