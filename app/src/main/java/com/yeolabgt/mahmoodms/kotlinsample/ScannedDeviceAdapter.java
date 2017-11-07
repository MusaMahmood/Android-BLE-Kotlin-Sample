package com.yeolabgt.mahmoodms.kotlinsample;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mahmoodms on 5/31/2016.
 *
 */

class ScannedDeviceAdapter extends ArrayAdapter<ScannedDevice> {
    private List<ScannedDevice> list;
    private LayoutInflater inflater;
    private int resId;
    //Constructor
    ScannedDeviceAdapter(Context context, int resId, List<ScannedDevice> objects) {
        super(context,resId,objects);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resId = resId;
        list = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ScannedDevice item = getItem(position);
        if (convertView == null) {
            convertView = inflater.inflate(resId,null);
        }
        TextView deviceNameTextview = convertView.findViewById(R.id.device_name);
        TextView deviceAddress = convertView.findViewById(R.id.device_address);
        TextView deviceRSSI = convertView.findViewById(R.id.device_rssi);
        deviceNameTextview.setText(item.getDisplayName());
        deviceAddress.setText(item.getDevice().getAddress());
        String currentRSSI = item.getRssi()+" dB";
        deviceRSSI.setText(currentRSSI);
        return convertView;
    }

    /**
     * add or update BluetoothDevice
     */
    public void update(BluetoothDevice newDevice, int rssi, byte[] scanRecord) {
        if ((newDevice==null)||(newDevice.getAddress()==null)) return;
        boolean contains = false;
        for (ScannedDevice device: list) {
            if (newDevice.getAddress().equalsIgnoreCase(device.getDevice().getAddress())) {
                contains = true;
                device.setRssi(rssi);//update
                break;
            }
        }
        if(!contains) {
            list.add(new ScannedDevice(newDevice, rssi));
        }
    }

    void update(BluetoothDevice newDevice, int rssi, ScanRecord scanRecord) {
        if ((newDevice==null)||(newDevice.getAddress()==null)) return;
        boolean contains = false;
        for (ScannedDevice device: list) {
            if (newDevice.getAddress().equalsIgnoreCase(device.getDevice().getAddress())) {
                contains = true;
                device.setRssi(rssi);//update
                break;
            }
        }

        if(!contains) {
            list.add(new ScannedDevice(newDevice, rssi));
        }
    }

    public void add(ScannedDevice scannedDevice) {
        if(!list.contains(scannedDevice)) {
            list.add(scannedDevice);
        }
    }

    void remove(int index) {
        list.remove(index);
    }
}
