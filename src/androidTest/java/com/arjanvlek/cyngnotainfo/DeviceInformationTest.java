package com.arjanvlek.cyngnotainfo;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.robotium.solo.Solo;

import java.util.Locale;

public class DeviceInformationTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    public final static String LOCALE_DUTCH = "Nederlands";

    public DeviceInformationTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception{
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

    public void testDeviceInformation() throws Exception{
        String appLocale = Locale.getDefault().getDisplayLanguage();

        // Go to Device Information tab
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (appLocale.equals(LOCALE_DUTCH)) {
                solo.clickOnText("Apparaat-informatie");
            } else {
                solo.clickOnText("Device information");
            }
        } else {
            if(appLocale.equals(LOCALE_DUTCH)) {
                solo.clickOnText("Apparaat-info");
            } else {
                solo.clickOnText("Device info");
            }
        }

        // We can only test the Android system version, as the other variables vary per device or emulator.
        TextView headerView = (TextView)solo.getView(R.id.device_information_header);
        TextView socView = (TextView)solo.getView(R.id.device_information_soc_field);
        TextView cpuFreqView = (TextView)solo.getView(R.id.device_information_cpu_freq_field);
        TextView memoryView = (TextView)solo.getView(R.id.device_information_memory_field);
        TextView osVerView = (TextView)solo.getView(R.id.device_information_os_ver_field);
        TextView serialNumberView = (TextView)solo.getView(R.id.device_information_serial_number_field);
        AdView adView = (AdView) solo.getView(R.id.device_information_banner_field);

        String deviceName;
        switch (Build.MODEL) {
            case "A0001":
                deviceName = "One";
                break;
            case "YUREKA":
                deviceName = "Yureka";
                break;
            case "N1":
                deviceName = "N1 CM Edition";
                break;
            default:
                deviceName = Build.MODEL;
        }

        long totalMemory;
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getActivity().getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            totalMemory = mi.totalMem / 1048576L;
        } else {
            totalMemory = 1;
        }
        String memory = totalMemory + " MB";

        assertEquals(View.VISIBLE, headerView.getVisibility());
        assertEquals(View.VISIBLE, socView.getVisibility());
        assertEquals(View.VISIBLE, cpuFreqView.getVisibility());
        // On Android versions lower than 3.0 "Honeycomb", the available memory amount is not displayed
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            assertEquals(View.VISIBLE, memoryView.getVisibility());
        }
        assertEquals(View.VISIBLE, osVerView.getVisibility());
        assertEquals(View.VISIBLE, serialNumberView.getVisibility());
        assertEquals(View.VISIBLE, adView.getVisibility());

        assertEquals(Build.MANUFACTURER + " " + deviceName, headerView.getText());
        assertEquals(Build.BOARD, socView.getText());
        assertNotNull(cpuFreqView.getText()); //TODO test this reliably on all devices and emulators
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            assertEquals(memory, memoryView.getText());
        }
        assertEquals(Build.VERSION.RELEASE, osVerView.getText());
        assertEquals(Build.SERIAL, serialNumberView.getText());
        assertEquals(getActivity().getString(R.string.device_information_advertising_id), adView.getAdUnitId());
    }
}
