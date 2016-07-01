package com.arjanvlek.cyngnotainfo.Model;

import com.arjanvlek.cyngnotainfo.Support.UpdateDownloader;
import com.arjanvlek.cyngnotainfo.Support.UpdateDownloader.DownloadSpeedUnits;

import java.util.Locale;

import static com.arjanvlek.cyngnotainfo.Support.UpdateDownloader.NOT_SET;

public class DownloadProgressData {

    private double downloadSpeed;
    private DownloadSpeedUnits speedUnits;
    private String timeRemaining;

    private int progress;


    public DownloadProgressData(double downloadSpeed, DownloadSpeedUnits speedUnits, long numberOfSecondsRemaining, int progress) {
        this.downloadSpeed = downloadSpeed;
        this.speedUnits = speedUnits;
        this.timeRemaining = calculateTimeRemaining(numberOfSecondsRemaining);
        this.progress = progress;
    }

    public double getDownloadSpeed() {
        return downloadSpeed;
    }

    public DownloadSpeedUnits getSpeedUnits() {
        return speedUnits;
    }

    public String getTimeRemaining() {
        return timeRemaining;
    }

    public int getProgress() {
        return progress;
    }

    private String calculateTimeRemaining(long numberOfSecondsRemaining) {
        if(numberOfSecondsRemaining == NOT_SET) {
            return null;
        }

        return String.format(Locale.getDefault(), "%02d:%02d", numberOfSecondsRemaining / 60, numberOfSecondsRemaining % 60);
    }
}
