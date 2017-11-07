package com.yeolabgt.mahmoodms.kotlinsample

import android.bluetooth.BluetoothDevice

/**
 * Created by mahmoodms on 5/31/2016.
 * Class for holding scanned device Bluetooth LE data.
 */

internal class ScannedDevice(
        /**
         * BluetoothDevice
         */
        val device: BluetoothDevice?,
        /**
         * RSSI
         */
        var rssi: Int) {
    /**
     * Display Name
     */
    var displayName: String? = null
        private set
    /**
     * Device MAC Address
     */
    //    public void setDisplayName(String displayName) {
    //        deviceDisplayName = displayName;
    //    }

    val deviceMac: String

    init {
        if (device == null) {
            throw IllegalArgumentException("BluetoothDevice == Null")
        }
        displayName = device.name
        if (displayName == null || displayName!!.length == 0) {
            displayName = UNKNOWN
        }
        deviceMac = device.address
    }

    companion object {
        private val UNKNOWN = "Unknown"
    }

    //    public void setDeviceMac(String deviceAddress) {
    //        deviceDiplayAddress = deviceAddress;
    //    }
}
