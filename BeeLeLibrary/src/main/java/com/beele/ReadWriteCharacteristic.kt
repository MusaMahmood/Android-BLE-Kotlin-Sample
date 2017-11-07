package com.beele

import android.bluetooth.BluetoothGatt


/**
 * ReadWriteCharacteristic.java
 * Model class that provides details about RequestType, BluetoothGatt object and
 * Object
 */

class ReadWriteCharacteristic

 constructor(val requestType: Int,

             val bluetoothGatt: BluetoothGatt,

             var `object`: Any?)
