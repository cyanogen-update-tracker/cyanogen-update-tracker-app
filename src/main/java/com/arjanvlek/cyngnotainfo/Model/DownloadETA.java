package com.arjanvlek.cyngnotainfo.Model;

import com.arjanvlek.cyngnotainfo.Support.UpdateDownloader.DownloadSpeedUnits;

public class DownloadETA {

    private double downloadSpeed;
    private DownloadSpeedUnits speedUnits;
    private long numberOfSecondsRmaining;

    private int progress;


    public DownloadETA(double downloadSpeed, DownloadSpeedUnits speedUnits, long numberOfSecondsRemaining, int progress) {
        this.downloadSpeed = downloadSpeed;
        this.speedUnits = speedUnits;
        this.numberOfSecondsRmaining = numberOfSecondsRemaining;
        this.progress = progress;
    };

    public double getDownloadSpeed() {
        return downloadSpeed;
    }

    public DownloadSpeedUnits getSpeedUnits() {
        return speedUnits;
    }

    public long getNumberOfSecondsRmaining() {
        return numberOfSecondsRmaining;
    }

    public int getProgress() {
        return progress;
    }
}
