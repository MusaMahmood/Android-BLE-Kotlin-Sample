package com.yeolabgt.mahmoodms.kotlinsample;

import android.bluetooth.BluetoothDevice;

/**
 * Created by mahmoodms on 5/31/2016.
 * Class for holding scanned device Bluetooth LE data.
 */

class ScannedDevice {
    private static final String UNKNOWN = "Unknown";
    /**
     * BluetoothDevice
     */
    private BluetoothDevice bluetoothDevice;
    /**
     * RSSI
     */
    private int rssiValue;
    /**
     * Display Name
     */
    private String deviceDisplayName;
    /**
     * Device MAC Address
     */
    private String deviceDiplayAddress;

    ScannedDevice(BluetoothDevice device, int rssi) {
        if(device == null) {
            throw new IllegalArgumentException("BluetoothDevice == Null");
        }
        bluetoothDevice = device;
        deviceDisplayName = device.getName();
        if((deviceDisplayName==null)||(deviceDisplayName.length()==0)) {
            deviceDisplayName = UNKNOWN;
        }
        rssiValue = rssi;
        deviceDiplayAddress = device.getAddress();
    }

    BluetoothDevice getDevice() {
        return bluetoothDevice;
    }

    int getRssi() {
        return rssiValue;
    }

    void setRssi(int rssi) {
        rssiValue = rssi;
    }

    String getDisplayName() {
        return deviceDisplayName;
    }

//    public void setDisplayName(String displayName) {
//        deviceDisplayName = displayName;
//    }

    String getDeviceMac() {
        return deviceDiplayAddress;
    }

//    public void setDeviceMac(String deviceAddress) {
//        deviceDiplayAddress = deviceAddress;
//    }
}
