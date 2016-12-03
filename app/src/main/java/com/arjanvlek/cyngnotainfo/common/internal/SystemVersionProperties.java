package com.arjanvlek.cyngnotainfo.common.internal;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class SystemVersionProperties {

    public enum SystemType {
        COS, CM, UNKNOWN;

        static SystemType of(String rawType) {
            return rawType.equals("RELEASE") ? COS : (rawType.equals("NIGHTLY") || rawType.equals("SNAPSHOT") || rawType.equals("MILESTONE") || rawType.equals("EXPERIMENTAL")) ? CM : UNKNOWN;
        }
    }

    private final SystemType systemType;

    private final String cyanogenDeviceName; // Ro.cm.device in Build.prop
    private final String cyanogenVersion; // ro.cm.display.version
    private final String securityPatchDate; // ro.build.version.security_patch (is here only for lollipop users, because the Android SDK says it requires Marshmallow or higher).

    public static final String NOT_SET = "not_set";


    SystemVersionProperties() {
        SystemType systemType = SystemType.UNKNOWN;
        String cyanogenOSVersion = NOT_SET;
        String cyanogenDeviceCodeName = NOT_SET;
        String systemTypeRaw;
        String securityPatchDate = NOT_SET;


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

                if (inputLine.contains("ro.cm.releasetype")) {
                    systemTypeRaw = inputLine.replace("[ro.cm.releasetype]: ", "");
                    systemTypeRaw = systemTypeRaw.replace("[", "");
                    systemTypeRaw = systemTypeRaw.replace("]", "");
                    systemType = SystemType.of(systemTypeRaw);
                }

                if (securityPatchDate.equals(NOT_SET)) {
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
        this.cyanogenDeviceName = cyanogenDeviceCodeName;
        this.cyanogenVersion = cyanogenOSVersion;
        this.securityPatchDate = securityPatchDate;
        this.systemType = systemType;
    }

    public SystemType getSystemType() {
        return systemType;
    }

    public String getCyanogenDeviceName() {
        return cyanogenDeviceName;
    }

    public String getCyanogenVersion() {
        return cyanogenVersion;
    }

    public String getSecurityPatchDate() {
        return securityPatchDate;
    }
}
