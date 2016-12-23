package com.arjanvlek.cyngnotainfo.common.internal;

import com.arjanvlek.cyngnotainfo.common.model.ServerParameters;
import com.arjanvlek.cyngnotainfo.cos.model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.cos.model.Device;
import com.arjanvlek.cyngnotainfo.cos.model.InstallGuideData;
import com.arjanvlek.cyngnotainfo.common.model.ServerMessage;
import com.arjanvlek.cyngnotainfo.common.model.UpdateMethod;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.arjanvlek.cyngnotainfo.common.internal.ApplicationData.APP_USER_AGENT;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.DEVICES;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.INSTALL_GUIDE;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.MOST_RECENT_UPDATE_DATA;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.REGISTER_DEVICE;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.SERVER_MESSAGES;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.SERVER_PARAMETERS;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.UPDATE_DATA;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.UPDATE_METHODS;

public class ServerConnector {

    private static final String USER_AGENT_HEADER = "User-Agent";

    private ObjectMapper objectMapper;

    public ServerConnector() {
        this.objectMapper = new ObjectMapper();
    }

    public List<Device> getDevices() {
        return findMultipleFromServerResponse(fetchDataFromServer(DEVICES, 20), Device.class);
    }

    public CyanogenOTAUpdate getCyanogenOTAUpdate(Long deviceId, Long updateMethodId, String incrementalSystemVersion) {
        return findOneFromServerResponse(fetchDataFromServer(UPDATE_DATA, 15, deviceId.toString(), updateMethodId.toString(), incrementalSystemVersion), CyanogenOTAUpdate.class);
    }

    public CyanogenOTAUpdate getMostRecentCyanogenOTAUpdate(Long deviceId, Long updateMethodId) {
        return findOneFromServerResponse(fetchDataFromServer(MOST_RECENT_UPDATE_DATA, 10, deviceId.toString(), updateMethodId.toString()), CyanogenOTAUpdate.class);
    }

    public List<UpdateMethod> getUpdateMethods(Long deviceId) {
        return findMultipleFromServerResponse(fetchDataFromServer(UPDATE_METHODS, 20, deviceId.toString()), UpdateMethod.class);
    }

    public ServerParameters getServerParameters() {
        return findOneFromServerResponse(fetchDataFromServer(SERVER_PARAMETERS, 10), ServerParameters.class);
    }

    public List<ServerMessage> getServerMessages(Long deviceId, Long updateMethodId) {
        return findMultipleFromServerResponse(fetchDataFromServer(SERVER_MESSAGES, 20, deviceId.toString(), updateMethodId.toString()), ServerMessage.class);
    }

    public InstallGuideData fetchInstallGuidePageFromServer(Long deviceId, Long updateMethodId, Integer pageNumber) {
        return findOneFromServerResponse(fetchDataFromServer(INSTALL_GUIDE, 10, deviceId.toString(), updateMethodId.toString(), pageNumber.toString()), InstallGuideData.class);
    }

    public URL getDeviceRegistrationURL() {
        try {
            return REGISTER_DEVICE.getURL();
        } catch (MalformedURLException e) {
            return null;
        }
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

    private String fetchDataFromServer(ServerRequest request, int timeoutInSeconds, String... params) {

        try {
            URL requestUrl = request.getURL(params);

            HttpURLConnection urlConnection = (HttpURLConnection) requestUrl.openConnection();

            int timeOutInMilliseconds = timeoutInSeconds * 1000;

            //setup request
            urlConnection.setRequestProperty(USER_AGENT_HEADER, APP_USER_AGENT);
            urlConnection.setConnectTimeout(timeOutInMilliseconds);
            urlConnection.setReadTimeout(timeOutInMilliseconds);

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

