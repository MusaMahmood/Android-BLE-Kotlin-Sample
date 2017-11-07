package com.beele

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log

import java.util.HashMap
import java.util.UUID

/**
 * BluetoothLeService.java
 *
 *
 * The communication between the Bluetooth Low Energy device will be communicated through this service class only
 * The initial connect request and disconnect request will be executed in this class.
 * Also, all the status from the Bluetooth device will be notified in the corresponding callback methods.
 */
class BluetoothLe(private val context: Context, private val mBluetoothManager: BluetoothManager?, private val mBluetoothLeListener: BluetoothLeListener) {
    private val processQueueExecutor = ProcessQueueExecutor()
    // To add and maintain the BluetoothGatt object of each BLE device.
    private val bluetoothGattHashMap = HashMap<String, BluetoothGatt>()
    // The connection status of the Blue tooth Low energy Device will be
    // notified in the below callback.
    private val mGattCallbacks = object : BluetoothGattCallback() {

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            mBluetoothLeListener.onServicesDiscovered(gatt, status)
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            mBluetoothLeListener.onReadRemoteRssi(gatt, rssi, status)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            mBluetoothLeListener.onCharacteristicChanged(gatt, characteristic)
        }


        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            mBluetoothLeListener.onConnectionStateChange(gatt, status, newState)
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            mBluetoothLeListener.onCharacteristicRead(gatt, characteristic, status)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            mBluetoothLeListener.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            mBluetoothLeListener.onDescriptorRead(gatt, descriptor, status)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            mBluetoothLeListener.onDescriptorWrite(gatt, descriptor, status)
        }

    }

    init {
        if (!processQueueExecutor.isAlive) {
            processQueueExecutor.start()
        }
    }

    /**
     * To read the value from the BLE Device
     *
     * @param mGatt          BluetoothGatt object of the device.
     * @param characteristic BluetoothGattCharacteristic of the device.
     */
    fun readCharacteristic(mGatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val readWriteCharacteristic = ReadWriteCharacteristic(ProcessQueueExecutor.REQUEST_TYPE_READ_CHAR, mGatt, characteristic)
        ProcessQueueExecutor.addProcess(readWriteCharacteristic)
    }

    /**
     * To write the value to BLE Device
     *
     * @param mGatt          BluetoothGatt object of the device.
     * @param characteristic BluetoothGattCharacteristic of the device.
     * @param b              value to write on to the BLE device.
     */
    fun writeCharacteristic(mGatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, b: ByteArray) {
        characteristic.value = b
        val readWriteCharacteristic = ReadWriteCharacteristic(ProcessQueueExecutor.REQUEST_TYPE_WRITE_CHAR, mGatt, characteristic)
        ProcessQueueExecutor.addProcess(readWriteCharacteristic)
    }

    /**
     * To read the descriptor value from the BLE Device
     *
     * @param mGatt          BluetoothGatt object of the device.
     * @param characteristic BluetoothGattCharacteristic of the device.
     */
    fun readDescriptor(mGatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val readWriteCharacteristic = ReadWriteCharacteristic(ProcessQueueExecutor.REQUEST_TYPE_READ_DESCRIPTOR, mGatt, characteristic)
        ProcessQueueExecutor.addProcess(readWriteCharacteristic)
    }

    /**
     * To write the descriptor value to BLE Device
     *
     * @param mGatt          BluetoothGatt object of the device.
     * @param characteristic BluetoothGattCharacteristic of the device.
     * @param b              value to write on to the BLE device.
     */
    fun writeDescriptor(mGatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, b: ByteArray) {
        characteristic.value = b
        val readWriteCharacteristic = ReadWriteCharacteristic(ProcessQueueExecutor.REQUEST_TYPE_WRITE_DESCRIPTOR, mGatt, characteristic)
        ProcessQueueExecutor.addProcess(readWriteCharacteristic)
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    fun setCharacteristicNotification(mGatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, enabled: Boolean) {
        if (!mGatt.setCharacteristicNotification(characteristic, enabled)) {
            return
        }
        val clientConfig = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG) ?: return
        clientConfig.value = if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        val readWriteCharacteristic = ReadWriteCharacteristic(ProcessQueueExecutor.REQUEST_TYPE_WRITE_DESCRIPTOR, mGatt, clientConfig)
        ProcessQueueExecutor.addProcess(readWriteCharacteristic)
    }

    /**
     * Connect bluetooth gatt.
     *
     * @param device      the device
     * @param autoConnect the auto connect
     * @return the bluetooth gatt
     */
    fun connect(device: BluetoothDevice?, autoConnect: Boolean): BluetoothGatt? {
        if (mBluetoothManager == null) {
            mBluetoothLeListener.onError("BluetoothManager is null")
        }
        if (device == null) {
            mBluetoothLeListener.onError("BluetoothDevice is null")
        }
        val bluetoothGatt = bluetoothGattHashMap[device!!.address]
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect()
            bluetoothGatt.close()
        }
        val connectionState = mBluetoothManager!!.getConnectionState(device, BluetoothProfile.GATT)
        val mBluetoothGatt: BluetoothGatt?
        if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
            mBluetoothGatt = device.connectGatt(context, autoConnect, mGattCallbacks)
            // Add the each BluetoothGatt in to an array list.
            if (!bluetoothGattHashMap.containsKey(device.address)) {
                bluetoothGattHashMap.put(device.address, mBluetoothGatt)
            } else {
                bluetoothGattHashMap.remove(device.address)
                bluetoothGattHashMap.put(device.address, mBluetoothGatt)
            }
        } else {
            mBluetoothGatt = null
        }
        return mBluetoothGatt
    }

    /**
     * Disconnect bluetooth gatt.
     *
     * @param mBluetoothGatt the BluetoothGatt of the device.
     */
    fun disconnect(mBluetoothGatt: BluetoothGatt) {
        try {
            bluetoothGattHashMap.remove(mBluetoothGatt.device.address)
            mBluetoothGatt.disconnect()
            mBluetoothGatt.close()
        } catch (e: Exception) {
            Log.e("BluetoothLe.class", "Exception" + e.toString())
        }

    }

    // Destructor
    @Throws(Throwable::class)
    protected fun finalize() {
        processQueueExecutor.interrupt()
    }

    interface BluetoothLeListener {
        //Core methods.
        fun onServicesDiscovered(gatt: BluetoothGatt, status: Int)

        fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int)

        fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int)

        fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic)

        //Read / Write Response method.
        fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int)

        fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int)

        fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int)

        fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int)

        fun onError(errorMessage: String)
    }

    companion object {
        private val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
