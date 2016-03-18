package com.arjanvlek.cyngnotainfo.Model;

import android.os.Build;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;

public class DeviceInformationData {
    private String DeviceManufacturer;
    private String deviceName;
    private String SOC;
    private String CPU_Frequency;
    private String OSVersion;
    private String SerialNumber;
    public static String UNKNOWN = "-";

    public DeviceInformationData() {
        setOSVersion();
        setCPU_Frequency(null);
        setSerialNumber();
        setDeviceManufacturer(null);
        setDeviceName(null);
        setSOC(null);
    }

    public String getDeviceManufacturer() {
        return DeviceManufacturer;
    }

    public void setDeviceManufacturer(String deviceManufacturer) {
        if (deviceManufacturer != null) {
            this.DeviceManufacturer = deviceManufacturer;
        } else {
            DeviceManufacturer = Build.MANUFACTURER;
        }
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        if (deviceName != null) {
            this.deviceName = deviceName;
        } else {
            this.deviceName = Build.DEVICE;
        }
    }

    public String getSOC() {
        return SOC;
    }

    public void setSOC(String SOC) {
        if (SOC != null) {
            this.SOC = SOC;
        } else {
            this.SOC = Build.BOARD;
        }
    }

    public String getCPU_Frequency() {
        return CPU_Frequency;
    }

    public void setCPU_Frequency(String CPU_Frequency) {
        if (CPU_Frequency != null) {
            this.CPU_Frequency = CPU_Frequency;
        } else {
            String cpuMaxFreq = "";
            RandomAccessFile reader = null;
            try {
                reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r");
            } catch (FileNotFoundException e) {
                this.CPU_Frequency = UNKNOWN;
            }
            try {
                if (reader != null) {
                    cpuMaxFreq = reader.readLine();
                }
            } catch (IOException e) {
                this.CPU_Frequency = UNKNOWN;
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                this.CPU_Frequency = UNKNOWN;
            }
            int cpuFreqInt = 0;
            try {
                cpuFreqInt = Integer.parseInt(cpuMaxFreq);
            } catch (NumberFormatException e) {
                this.CPU_Frequency = UNKNOWN;
            }
            if (cpuFreqInt != 0) {
                int cpuFreqMhz = cpuFreqInt / 1000;
                BigDecimal cpuFreqMhz2 = new BigDecimal(cpuFreqMhz);

                BigDecimal cpuFreqGhz = cpuFreqMhz2.divide(new BigDecimal(1000), 3, BigDecimal.ROUND_DOWN);
                this.CPU_Frequency = cpuFreqGhz.toString();
            }
        }
    }

    public String getOSVersion() {
        return OSVersion;
    }

    public void setOSVersion() {
        this.OSVersion = Build.VERSION.RELEASE;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public void setSerialNumber() {
        SerialNumber = Build.SERIAL;
    }
}
