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
    private String memoryAmount;
    private String OSVersion;
    private String SerialNumber;
    private String OSCodeName;
    public static String UNKNOWN = "Unknown";

    public DeviceInformationData() {
        setOSVersion(null);
        setCPU_Frequency(null);
        setSerialNumber(null);
        setDeviceManufacturer(null);
        setDeviceName(null);
        setOSCodeName(null);
        setSOC(null);
        setMemoryAmount(null);
    }
    public String getOSCodeName() {
        return OSCodeName;
    }
    public void setOSCodeName(String osCodeName) {
        if(osCodeName != null) {
            this.OSCodeName = osCodeName;
        }
        else {
            this.OSCodeName = Build.VERSION.INCREMENTAL;;
        }

    }

    public String getDeviceManufacturer() {
        return DeviceManufacturer;
    }

    public void setDeviceManufacturer(String deviceManufacturer) {
        if(deviceManufacturer != null) {
            this.DeviceManufacturer = deviceManufacturer;
        }
        else {
            DeviceManufacturer = Build.MANUFACTURER;
        }
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        if(deviceName != null) {
            this.deviceName = deviceName;
        }

        else {
            switch (Build.MODEL) {
                case "A0001":
                    this.deviceName = "One";
                    break;
                case "YUREKA":
                    this.deviceName = "Yureka";
                    break;
                case "N1":
                    this.deviceName = "N1 CM Edition";
                    break;
                default:
                    this.deviceName = Build.MODEL;
            }
        }
    }

    public String getSOC() {
        return SOC;
    }

    public void setSOC(String SOC) {
        if(SOC != null) {
            this.SOC = SOC;
        }
        else {
            this.SOC = Build.BOARD;
        }
    }

    public String getCPU_Frequency() {
        return CPU_Frequency;
    }

    public void setCPU_Frequency(String CPU_Frequency) {
        if(CPU_Frequency != null) {
            this.CPU_Frequency = CPU_Frequency;
        }
        else {
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
            int cpuFreqInt = Integer.parseInt(cpuMaxFreq);
            int cpuFreqMhz = cpuFreqInt / 1000;
            BigDecimal cpuFreqMhz2 = new BigDecimal(cpuFreqMhz);

            BigDecimal cpuFreqGhz = cpuFreqMhz2.divide(new BigDecimal(1000),3,BigDecimal.ROUND_DOWN);
            this.CPU_Frequency = cpuFreqGhz.toString();
        }
    }

    public String getMemoryAmount() {
        return memoryAmount;
    }

    public void setMemoryAmount(String memoryAmount) {
        String memoryAmount2 = null;
        String memoryAmount3 = null;
        if(memoryAmount != null) {
            this.memoryAmount = memoryAmount;
        }
        else {

            RandomAccessFile reader = null;
            try {
                reader = new RandomAccessFile("/proc/meminfo", "r");
            } catch (FileNotFoundException e) {
                this.memoryAmount = UNKNOWN;
            }
            try {
                if (reader != null) {
                    memoryAmount = reader.readLine();
                    try {
                        memoryAmount2 = memoryAmount.replace("MemTotal:        ", "");
                    }
                    catch (Exception ignored) {

                    }
                    try {
                        memoryAmount3 = memoryAmount2 != null ? memoryAmount2.replace(" kB", "") : null;
                    }
                    catch (Exception ignored) {

                    }
                    if(memoryAmount3 == null) {
                        if(memoryAmount2 == null) {
                            this.memoryAmount = memoryAmount;
                        }
                        else {
                            this.memoryAmount = memoryAmount2;
                        }
                    }
                    else {
                        int memoryInt = Integer.parseInt(memoryAmount3);
                        int memoryInt2 = memoryInt / 1000;
                        BigDecimal memoryMegabyte = new BigDecimal(memoryInt2);

                        BigDecimal memoryGigabyte = memoryMegabyte.divide(new BigDecimal(1000),2,BigDecimal.ROUND_DOWN);
                        this.memoryAmount = memoryGigabyte.toString();
                    }

                }
            } catch (IOException e) {
                this.memoryAmount = UNKNOWN;
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                this.memoryAmount = UNKNOWN;
            }
        }
    }

    public String getOSVersion() {
        return OSVersion;
    }

    public void setOSVersion(String OSVersion) {
        this.OSVersion = Build.VERSION.RELEASE;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        SerialNumber = Build.SERIAL;
    }
}
