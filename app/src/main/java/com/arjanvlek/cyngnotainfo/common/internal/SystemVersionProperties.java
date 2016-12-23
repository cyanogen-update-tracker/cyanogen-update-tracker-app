package com.arjanvlek.cyngnotainfo.common.internal;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class SystemVersionProperties {

    public enum SystemType {
        COS, OFFICIAL_CM, UNOFFICIAL_CM, UNKNOWN;

        private static final String TYPE_RELEASE = "RELEASE";
        private static final String TYPE_NIGHTLY = "NIGHTLY";
        private static final String TYPE_SNAPSHOT = "SNAPSHOT";
        private static final String TYPE_MILESTONE = "MILESTONE";
        private static final String TYPE_EXPERIMENTAL = "EXPERIMENTAL";
        private static final String TYPE_UNOFFICIAL = "UNOFFICIAL";
        private static final String OPPO_N1 = "n1";

        static SystemType of(final String rawType, final String cyanogenDeviceCodename) {
            if (rawType == null || rawType.isEmpty()) return UNKNOWN;
            final String type = rawType.toUpperCase();
            return isCos(type, cyanogenDeviceCodename) ? COS : isOfficialCm(type) ? OFFICIAL_CM : isUnofficialCm(type) ? UNOFFICIAL_CM : UNKNOWN;
        }

        static boolean isCos(final String systemType, final String cyanogenDeviceCodename) {
            // Cyanogen OS uses the RELEASE type, from CM11S onwards. The only exception is the Oppo N1 CyanogenMod edition, which had CyanogenOS based on CM10.2
            return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT || (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2 && cyanogenDeviceCodename.equalsIgnoreCase(OPPO_N1))) && systemType.equalsIgnoreCase(TYPE_RELEASE);
        }

        static boolean isOfficialCm (final String systemType) {
            // Until CM11, the Release type was used for stable CM builds. From CM11 onwards, the RELEASE type is only used for COS - and SNAPSHOT is used to mark a release as stable.
            return ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2 && systemType.equals(TYPE_RELEASE)) || systemType.equals(TYPE_NIGHTLY) || systemType.equals(TYPE_SNAPSHOT) || systemType.equals(TYPE_MILESTONE) || systemType.equals(TYPE_EXPERIMENTAL));
        }

        static boolean isUnofficialCm (final String systemType) {
            // Unofficial CM builds are unsupported in this app. Users running such versions must get a warning that their device is not supported
            return systemType.equals(TYPE_UNOFFICIAL);
        }
    }

    private final SystemType systemType;
    private final String cyanogenModChannel;

    private final String cyanogenDeviceName; // Ro.cm.device in Build.prop
    private final String cyanogenVersion; // ro.cm.display.version
    private final String securityPatchDate; // ro.build.version.security_patch (is here for lollipop users, because the Android SDK includes it on Marshmallow and higher only).

    public static final String NOT_SET = "not_set";


    SystemVersionProperties() {
        final SystemType systemType;
        String cyanogenOSVersion = NOT_SET;
        String cyanogenDeviceCodeName = NOT_SET;
        String systemTypeRaw = NOT_SET;
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
        systemType = SystemType.of(systemTypeRaw, cyanogenDeviceCodeName);

        this.cyanogenDeviceName = cyanogenDeviceCodeName;
        this.cyanogenVersion = cyanogenOSVersion;
        this.securityPatchDate = securityPatchDate;
        this.cyanogenModChannel = systemTypeRaw;
        this.systemType = systemType;
    }

    public SystemType getSystemType() {
        return systemType;
    }

    public String getCyanogenModChannel() {
        return cyanogenModChannel;
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
