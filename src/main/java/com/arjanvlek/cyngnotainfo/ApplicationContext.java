package com.arjanvlek.cyngnotainfo;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.arjanvlek.cyngnotainfo.Model.Device;
import com.arjanvlek.cyngnotainfo.Model.SystemVersionProperties;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import org.joda.time.LocalDateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ApplicationContext extends Application {
    private List<Device> devices;
    private LocalDateTime deviceFetchDate;
    private ServerConnector serverConnector;
    public static final String NO_CYANOGEN_OS = "no_cyanogen_os_ver_found";
    private static SystemVersionProperties SYSTEM_VERSION_PROPERTIES_INSTANCE;

    /**
     * Prevents the /devices request to be performed more than once by storing it in the Application class.
     * If the stored data is more than 5 minutes old, one new request is allowed and so on for each 5 minutes.
     * @return List of Devices that are enabled on the server.
     */
    public List<Device> getDevices() {
        LocalDateTime now = LocalDateTime.now();
        if(devices != null && deviceFetchDate != null && deviceFetchDate.plusMinutes(5).isAfter(now)) {
            return devices;
        }

        else {
            if(serverConnector == null) {
                serverConnector = new ServerConnector();
            }
            devices = serverConnector.getDevices();
            deviceFetchDate = LocalDateTime.now();
            return devices;
        }
    }

    public ServerConnector getServerConnector() {
        if(serverConnector == null) {
            serverConnector = new ServerConnector();
            return serverConnector;
        }
        else {
            return serverConnector;
        }
    }

    public SystemVersionProperties getSystemVersionProperties() {
        if(SYSTEM_VERSION_PROPERTIES_INSTANCE == null) {
            SystemVersionProperties systemVersionProperties = new SystemVersionProperties();
            String cyanogenOSVersion = NO_CYANOGEN_OS;
            String cyanogenDeviceCodeName = NO_CYANOGEN_OS;
            String securityPatchDate = NO_CYANOGEN_OS;
            try {
                Process getBuildPropProcess = new ProcessBuilder()
                        .command("getprop")
                        .redirectErrorStream(true)
                        .start();

                BufferedReader in = new BufferedReader(new InputStreamReader(getBuildPropProcess.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.contains("ro.cm.display.version")) {
                        cyanogenOSVersion = inputLine.replace("[ro.cm.display.version]: ", "");
                        cyanogenOSVersion = cyanogenOSVersion.replace("[", "");
                        cyanogenOSVersion = cyanogenOSVersion.replace("]", "");
                    }

                    if (inputLine.contains("ro.cm.device")) {
                        cyanogenDeviceCodeName = inputLine.replace("[ro.cm.device]: ", "");
                        cyanogenDeviceCodeName = cyanogenDeviceCodeName.replace("[", "");
                        cyanogenDeviceCodeName = cyanogenDeviceCodeName.replace("]", "");
                    }

                    if(securityPatchDate.equals(NO_CYANOGEN_OS)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            securityPatchDate = Build.VERSION.SECURITY_PATCH;
                        } else {
                            if (inputLine.contains("ro.build.version.security_patch")) {
                                securityPatchDate = inputLine.replace("[ro.build.version.security_patch]: ", "");
                                securityPatchDate = securityPatchDate.replace("[", "");
                                securityPatchDate = securityPatchDate.replace("]", "");
                            }
                        }
                    }
                }
                getBuildPropProcess.destroy();

            } catch (IOException e) {
                Log.e("IOException buildProp", e.getLocalizedMessage());
            }
            systemVersionProperties.setCyanogenDeviceCodeName(cyanogenDeviceCodeName);
            systemVersionProperties.setCyanogenOSVersion(cyanogenOSVersion);
            systemVersionProperties.setSecurityPatchDate(securityPatchDate);
            SYSTEM_VERSION_PROPERTIES_INSTANCE = systemVersionProperties;
            return SYSTEM_VERSION_PROPERTIES_INSTANCE;
        } else {
            return SYSTEM_VERSION_PROPERTIES_INSTANCE;
        }
    }
}
