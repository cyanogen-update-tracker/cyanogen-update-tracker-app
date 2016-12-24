package com.arjanvlek.cyngnotainfo.common.internal;

import com.arjanvlek.cyngnotainfo.cm.model.CyanogenModUpdateData;
import com.arjanvlek.cyngnotainfo.cm.model.CyanogenModUpdateDataRequest;
import com.arjanvlek.cyngnotainfo.cm.model.CyanogenModUpdateDataRequest.CMRequestParams;
import com.arjanvlek.cyngnotainfo.common.model.ServerParameters;
import com.arjanvlek.cyngnotainfo.cos.model.CyanogenOSUpdateData;
import com.arjanvlek.cyngnotainfo.cos.model.Device;
import com.arjanvlek.cyngnotainfo.cos.model.InstallGuideData;
import com.arjanvlek.cyngnotainfo.common.model.ServerMessage;
import com.arjanvlek.cyngnotainfo.common.model.UpdateMethod;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.arjanvlek.cyngnotainfo.common.internal.ApplicationData.APP_USER_AGENT;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.CM_API_URL;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.CM_UPDATE_DATA;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.DEVICES;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.INSTALL_GUIDE;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.MOST_RECENT_COS_UPDATE_DATA;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.REGISTER_DEVICE;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.SERVER_MESSAGES;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.SERVER_PARAMETERS;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.COS_UPDATE_DATA;
import static com.arjanvlek.cyngnotainfo.common.internal.ServerRequest.UPDATE_METHODS;

public class ServerConnector {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String METHOD_POST = "POST";

    private ObjectMapper objectMapper;

    public ServerConnector() {
        this.objectMapper = new ObjectMapper();
    }

    public List<Device> getDevices() {
        return findMultipleFromServerResponse(fetchDataFromServer(DEVICES, 20), Device.class);
    }

    public CyanogenOSUpdateData getCyanogenOSUpdateData(Long deviceId, Long updateMethodId, String incrementalSystemVersion) {
        return findOneFromServerResponse(fetchDataFromServer(COS_UPDATE_DATA, 15, deviceId.toString(), updateMethodId.toString(), incrementalSystemVersion), CyanogenOSUpdateData.class);
    }

    public CyanogenOSUpdateData getMostRecentCyanogenOSUpdateData(Long deviceId, Long updateMethodId) {
        return findOneFromServerResponse(fetchDataFromServer(MOST_RECENT_COS_UPDATE_DATA, 10, deviceId.toString(), updateMethodId.toString()), CyanogenOSUpdateData.class);
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

    private String getCyanogenModApiURLFromServer() {
        return fetchDataFromServer(CM_API_URL, 10);
    }

    public CyanogenModUpdateData getCyanogenModUpdateData(SystemVersionProperties systemVersionProperties) {
        CyanogenModUpdateDataRequest request = new CyanogenModUpdateDataRequest(
                new CMRequestParams(
                    systemVersionProperties.getCyanogenDeviceName(),
                    systemVersionProperties.getCyanogenModChannel()
                )
        );
        return findOneFromServerResponse(postFromServer(CM_UPDATE_DATA, 15, request, getCyanogenModApiURLFromServer()), CyanogenModUpdateData.class);
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

    private String postFromServer(ServerRequest request, int timeoutInSeconds, CyanogenModUpdateDataRequest postData, String...params) {

        try {
            URL requestUrl = request.getURL(params);

            HttpURLConnection urlConnection = (HttpURLConnection) requestUrl.openConnection();
            urlConnection.setRequestMethod(METHOD_POST);

            int timeOutInMilliseconds = timeoutInSeconds * 1000;

            //setup request
            urlConnection.setRequestProperty(USER_AGENT_HEADER, APP_USER_AGENT);
            urlConnection.setConnectTimeout(timeOutInMilliseconds);
            urlConnection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON);
            urlConnection.setReadTimeout(timeOutInMilliseconds);
            urlConnection.setDoOutput(true);

            OutputStream outputStream = urlConnection.getOutputStream();
            objectMapper.writeValue(outputStream, postData);
            outputStream.flush();
            outputStream.close();


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

