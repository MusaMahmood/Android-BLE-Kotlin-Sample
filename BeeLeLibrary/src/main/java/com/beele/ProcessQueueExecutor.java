package com.beele;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ProcessQueueExecutor.java
 * <p/>
 * This class is used to execute the read,write and write descriptor request one
 * by one based on EXECUTE_DELAY.
 */
public class ProcessQueueExecutor extends Thread {

    final static int REQUEST_TYPE_READ_CHAR = 1;
    final static int REQUEST_TYPE_WRITE_CHAR = 2;
    final static int REQUEST_TYPE_READ_DESCRIPTOR = 3;
    final static int REQUEST_TYPE_WRITE_DESCRIPTOR = 4;
    private final static int REQUEST_TYPE_NOTIFICATION_ON = 5;
    private static long EXECUTE_DELAY = 1000;// delay in execution ms
    private static List<ReadWriteCharacteristic> processList = new ArrayList<ReadWriteCharacteristic>();
    private Timer processQueueTimer = new Timer();

    /**
     * Adds the request to ProcessQueueExecutor
     *
     * @param readWriteCharacteristic description
     */
    static void addProcess(
            ReadWriteCharacteristic readWriteCharacteristic) {
        processList.add(readWriteCharacteristic);
    }

    /**
     * Removes the request from ProcessQueueExecutor
     *
     * @param readWriteCharacteristic description
     */
    public static void removeProcess(
            ReadWriteCharacteristic readWriteCharacteristic) {
        processList.remove(readWriteCharacteristic);
    }

    public void executeProcess() {

        if (!processList.isEmpty()) {
            ReadWriteCharacteristic readWriteCharacteristic = processList
                    .get(0);
            int type = readWriteCharacteristic.getRequestType();
            BluetoothGatt bluetoothGatt = readWriteCharacteristic
                    .getBluetoothGatt();
            Object parseObject = readWriteCharacteristic.getObject();

            if (type == REQUEST_TYPE_READ_CHAR) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) parseObject;
                try {
                    bluetoothGatt.readCharacteristic(characteristic);
                } catch (Exception e) {
                }
            } else if (type == REQUEST_TYPE_WRITE_CHAR) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) parseObject;
                try {
                    bluetoothGatt.writeCharacteristic(characteristic);
                } catch (Exception e) {
                }
            } else if (type == REQUEST_TYPE_READ_DESCRIPTOR) {
                BluetoothGattDescriptor clientConfig = (BluetoothGattDescriptor) parseObject;
                try {
                    bluetoothGatt.readDescriptor(clientConfig);
                } catch (Exception e) {
                }
            } else if (type == REQUEST_TYPE_WRITE_DESCRIPTOR) {
                BluetoothGattDescriptor clientConfig = (BluetoothGattDescriptor) parseObject;
                try {
                    bluetoothGatt.writeDescriptor(clientConfig);
                } catch (Exception e) {
                }
            } else if (type == REQUEST_TYPE_NOTIFICATION_ON) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) parseObject;
                try {
                    bluetoothGatt.setCharacteristicNotification(characteristic, true);
                } catch (Exception e) {

                }
            }

            removeProcess(readWriteCharacteristic);
        }
    }

    /**
     * Returns the number of elements in ProcessQueueExecutor
     *
     * @return the number of elements in ProcessQueueExecutor
     */
    public int getSize() {
        if (processList != null) {
            return processList.size();
        }
        return 0;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if (processQueueTimer != null) {
            processQueueTimer.cancel();
        }
    }

    @Override
    public void run() {
        super.run();
        processQueueTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                executeProcess();
            }
        }, 0, EXECUTE_DELAY);
    }
}
