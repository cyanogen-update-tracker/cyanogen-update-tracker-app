package com.arjanvlek.cyngnotainfo.Model;

import com.arjanvlek.cyngnotainfo.Support.UpdateDownloader.DownloadSpeedUnits;

public class DownloadProgressData {

    private double downloadSpeed;
    private DownloadSpeedUnits speedUnits;
    private TimeRemaining timeRemaining;

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

    public TimeRemaining getTimeRemaining() {
        return timeRemaining;
    }

    public int getProgress() {
        return progress;
    }


    public class TimeRemaining {
        private long minutes;
        private long seconds;

        public TimeRemaining(long minutes, long seconds) {
            this.minutes = minutes;
            this.seconds = seconds;
        }

        public long getMinutes() {
            return minutes;
        }

        public long getSeconds() {
            return seconds;
        }
    }

    private TimeRemaining calculateTimeRemaining(long numberOfSecondsRemaining) {
        long minutesRemaining = numberOfSecondsRemaining / 60;
        long secondsRemaining = numberOfSecondsRemaining - minutesRemaining * 60;

        return new TimeRemaining(minutesRemaining, secondsRemaining);
    }
}
