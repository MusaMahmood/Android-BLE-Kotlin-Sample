package com.beele

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

import java.util.ArrayList
import java.util.Timer
import java.util.TimerTask

/**
 * ProcessQueueExecutor.java
 *
 *
 * This class is used to execute the read,write and write descriptor request one
 * by one based on EXECUTE_DELAY.
 */
class ProcessQueueExecutor : Thread() {
    private val processQueueTimer = Timer()

    /**
     * Returns the number of elements in ProcessQueueExecutor
     *
     * @return the number of elements in ProcessQueueExecutor
     */
    val size: Int
        get() = processList.size

    fun executeProcess() {

        if (!processList.isEmpty()) {
            val readWriteCharacteristic = processList[0]
            val type = readWriteCharacteristic.requestType
            val bluetoothGatt = readWriteCharacteristic
                    .bluetoothGatt
            val parseObject = readWriteCharacteristic.`object`
            when (type) {
                REQUEST_TYPE_READ_CHAR -> {
                    val characteristic = parseObject as BluetoothGattCharacteristic?
                    try {
                        bluetoothGatt.readCharacteristic(characteristic)
                    } catch (e: Exception) {
                    }
                }
                REQUEST_TYPE_WRITE_CHAR -> {
                    val characteristic = parseObject as BluetoothGattCharacteristic?
                    try {
                        bluetoothGatt.writeCharacteristic(characteristic)
                    } catch (e: Exception) {
                    }
                }
                REQUEST_TYPE_READ_DESCRIPTOR -> {
                    val clientConfig = parseObject as BluetoothGattDescriptor?
                    try {
                        bluetoothGatt.readDescriptor(clientConfig)
                    } catch (e: Exception) {
                    }
                }
                REQUEST_TYPE_WRITE_DESCRIPTOR -> {
                    val clientConfig = parseObject as BluetoothGattDescriptor?
                    try {
                        bluetoothGatt.writeDescriptor(clientConfig)
                    } catch (e: Exception) {
                    }
                }
                REQUEST_TYPE_NOTIFICATION_ON -> {
                    val characteristic = parseObject as BluetoothGattCharacteristic?
                    try {
                        bluetoothGatt.setCharacteristicNotification(characteristic, true)
                    } catch (e: Exception) {
                    }
                }
            }

            removeProcess(readWriteCharacteristic)
        }
    }

    override fun interrupt() {
        super.interrupt()
        processQueueTimer.cancel()
    }

    override fun run() {
        super.run()
        processQueueTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                executeProcess()
            }
        }, 0, EXECUTE_DELAY)
    }

    companion object {

        val REQUEST_TYPE_READ_CHAR = 1
        val REQUEST_TYPE_WRITE_CHAR = 2
        val REQUEST_TYPE_READ_DESCRIPTOR = 3
        val REQUEST_TYPE_WRITE_DESCRIPTOR = 4
        private val REQUEST_TYPE_NOTIFICATION_ON = 5
        private val EXECUTE_DELAY: Long = 1000// delay in execution ms
        private val processList = ArrayList<ReadWriteCharacteristic>()

        /**
         * Adds the request to ProcessQueueExecutor
         *
         * @param readWriteCharacteristic description
         */
        fun addProcess(
                readWriteCharacteristic: ReadWriteCharacteristic) {
            processList.add(readWriteCharacteristic)
        }

        /**
         * Removes the request from ProcessQueueExecutor
         *
         * @param readWriteCharacteristic description
         */
        fun removeProcess(
                readWriteCharacteristic: ReadWriteCharacteristic) {
            processList.remove(readWriteCharacteristic)
        }
    }
}
