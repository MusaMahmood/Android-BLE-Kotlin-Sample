package com.beele;

import android.bluetooth.BluetoothGatt;


/**
 * ReadWriteCharacteristic.java
 * Model class that provides details about RequestType, BluetoothGatt object and
 * Object
 */

public class ReadWriteCharacteristic {

    private int requestType;
    private BluetoothGatt bluetoothGatt;
    private Object object;

//    public ReadWriteCharacteristic() {
//    }

    ReadWriteCharacteristic(int requestType, BluetoothGatt bluetoothGatt, Object object) {
        this.requestType = requestType;
        this.bluetoothGatt = bluetoothGatt;
        this.object = object;
    }

    int getRequestType() {
        return requestType;
    }

//    public void setRequestType(int requestType) {
//        this.requestType = requestType;
//    }

    BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

//    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
//        this.bluetoothGatt = bluetoothGatt;
//    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

}
