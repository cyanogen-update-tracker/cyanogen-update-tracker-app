package com.arjanvlek.cyngnotainfo;

import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

import com.arjanvlek.cyngnotainfo.Support.ServerConnector;
import com.robotium.solo.Solo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Tests the downloading of an update
 */
public class DownloadUpdateTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo;
    public final static String LOCALE_DUTCH = "Nederlands";


    public DownloadUpdateTest() {
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

    public void testDownloadingUpdate() throws Exception {
        String appLocale = Locale.getDefault().getDisplayLanguage();
        // wait for app to start
        solo.sleep(500);
        Button downloadButton;
        if(appLocale.equals(LOCALE_DUTCH)) {
            downloadButton = solo.getButton("Downloaden");
            assertTrue(downloadButton.isEnabled());
        }
        else {
            downloadButton = solo.getButton("Download");
            assertTrue(downloadButton.isEnabled());
        }

        solo.clickOnView(downloadButton);
        solo.sleep(2000);

        File downloadedFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/downloadTest.txt");
        assertNotNull(downloadedFile);
        FileInputStream fIn = new FileInputStream(downloadedFile);
        BufferedReader myReader = new BufferedReader(
                new InputStreamReader(fIn));
        String aDataRow;
        String aBuffer = "";
        while ((aDataRow = myReader.readLine()) != null) {
            aBuffer += aDataRow + "\n";
        }
        assertEquals("Thanks for testing :)\n", aBuffer);

        // Delete the downloaded file
        assertTrue(downloadedFile.delete());

        // Verify that the file is indeed gone
        try {
            // This *should* give an exception ("file not found").
            downloadedFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/downloadTest.txt");
            new FileInputStream(downloadedFile);

        } catch (Exception ignored) {
            downloadedFile = null;
        }
        assertNull(downloadedFile);
    }
}
