package com.arjanvlek.cyngnotainfo.views;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.arjanvlek.cyngnotainfo.ApplicationContext;
import com.arjanvlek.cyngnotainfo.Model.SystemVersionProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public abstract class AbstractFragment extends Fragment{

    private ApplicationContext applicationContext;
    //Test devices for ads.
    public static final String ADS_TEST_DEVICE_ID_OWN_DEVICE = "7CFCF353FBC40363065F03DFAC7D7EE4";
    public static final String ADS_TEST_DEVICE_ID_EMULATOR_1 = "D9323E61DFC727F573528DB3820F7215";
    public static final String ADS_TEST_DEVICE_ID_EMULATOR_2 = "D732F1B481C5274B05D707AC197B33B2";
    public static final String ADS_TEST_DEVICE_ID_EMULATOR_3 = "3CFEF5EDED2F2CC6C866A48114EA2ECE";
    public static final String NO_CYANOGEN_OS = "no_cyanogen_os_ver_found";
    public static SystemVersionProperties SYSTEM_VERSION_PROPERTIES;

    public ApplicationContext getApplicationContext() {
        if(applicationContext == null) {
            applicationContext = (ApplicationContext)getActivity().getApplication();
        }
        return applicationContext;
    }

    protected SystemVersionProperties getSystemVersionProperties() {
        if(SYSTEM_VERSION_PROPERTIES == null) {
            SystemVersionProperties systemVersionProperties = new SystemVersionProperties();
            String cyanogenOSVersion = NO_CYANOGEN_OS;
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
                getBuildPropProcess.destroy();

            } catch (IOException e) {
                Log.e("IOException buildProp", e.getLocalizedMessage());
            }
            systemVersionProperties.setCyanogenOSVersion(cyanogenOSVersion);
            systemVersionProperties.setSecurityPatchDate(securityPatchDate);
            systemVersionProperties.setModelNumber(Build.DEVICE);
            SYSTEM_VERSION_PROPERTIES = systemVersionProperties;
            return SYSTEM_VERSION_PROPERTIES;
        } else {
            return SYSTEM_VERSION_PROPERTIES;
        }
    }
}
