package com.newstar.scorpiodata.risk;

public class OtherRiskInfo {
        public String imei;
        public String imsi;
        public String phoneNumber;
        public String carrierName;

        public String phoneModel;
        public String uid;
        public String wifiMacAddress;
        public String systemVersion;
        public String ipv4Address;
        public String GAID;
        public String availableSize;
        public String totalSize;
        public String bootTime;
        public String isRobot;
        //设备总运行内存
        public String totalMemory;
        // 设备总可用内存
        public String availMemory;
        //屏幕分辨率
        public String screenSize;
        //设备语言
        public String language;

        @Override
        public String toString() {
            return "OtherRiskInfo{" +
                    "imei='" + imei + '\'' +
                    ", imsi='" + imsi + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", carrierName='" + carrierName + '\'' +
                    ", phoneModel='" + phoneModel + '\'' +
                    ", uid='" + uid + '\'' +
                    ", wifiMacAddress='" + wifiMacAddress + '\'' +
                    ", systemVersion='" + systemVersion + '\'' +
                    ", ipv4Address='" + ipv4Address + '\'' +
                    ", GAID='" + GAID + '\'' +
                    ", availableSize='" + availableSize + '\'' +
                    ", totalSize='" + totalSize + '\'' +
                    ", totalSize='" + bootTime + '\'' +
                    '}';
        }
    }