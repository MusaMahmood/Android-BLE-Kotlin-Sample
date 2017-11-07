package com.yeolabgt.mahmoodms.androidblekotlin;

import java.util.UUID;

/**
 * Created by mahmoodms on 6/30/2016.
 * Constants
 */
public final class AppConstant {
    // To prevent someone from accidentally instantiating the AppConstant class,
    // give it an empty constructor.
    public AppConstant() {
    }

    /**
     * For new EMG:
     */
    static final UUID SERVICE_3CH_EMG_SIGNAL = UUID.fromString("228b3460-35fd-875f-39fe-b2a394d28057");
    //Characteristic2:
    static final UUID CHAR_3CH_EMG_SIGNAL_CH1 = UUID.fromString("00003461-0000-1000-8000-00805f9b34fb");
    static final UUID CHAR_3CH_EMG_SIGNAL_CH2 = UUID.fromString("00003462-0000-1000-8000-00805f9b34fb");
    static final UUID CHAR_3CH_EMG_SIGNAL_CH3 = UUID.fromString("00003463-0000-1000-8000-00805f9b34fb");
    /**
     * For EOG (new)
     */
    static final UUID SERVICE_EOG_SIGNAL = UUID.fromString("228be0f0-35fd-875f-39fe-b2a394d28057");//E0F0
    static final UUID CHAR_EOG_CH1_SIGNAL = UUID.fromString("0000e0f1-0000-1000-8000-00805f9b34fb");
    static final UUID CHAR_EOG_CH2_SIGNAL = UUID.fromString("0000e0f2-0000-1000-8000-00805f9b34fb");
    static final UUID CHAR_EOG_CH3_SIGNAL = UUID.fromString("0000e0f3-0000-1000-8000-00805f9b34fb");
    /**
     * For EEG:
     */
    static final UUID SERVICE_EEG_SIGNAL = UUID.fromString("228beef0-35fd-875f-39fe-b2a394d28057");
    //Characteristic:
    static final UUID CHAR_EEG_CH1_SIGNAL = UUID.fromString("0000eef1-0000-1000-8000-00805f9b34fb");
    static final UUID CHAR_EEG_CH2_SIGNAL = UUID.fromString("0000eef2-0000-1000-8000-00805f9b34fb");
    static final UUID CHAR_EEG_CH3_SIGNAL = UUID.fromString("0000eef3-0000-1000-8000-00805f9b34fb");
    static final UUID CHAR_EEG_CH4_SIGNAL = UUID.fromString("0000eef4-0000-1000-8000-00805f9b34fb");

    /** TO READ THE BLE DEVICE'S INFORMATION **/

    // To read the device information for the device information service.
    static UUID SERVICE_DEVICE_INFO = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    // Characteristic UUID to read the each BLE Device's serial number.
    static UUID CHAR_SERIAL_NUMBER = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");
    // Characteristic UUID to read the BLE Device's software version.
    static UUID CHAR_SOFTWARE_REV = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");

    /** TO READ THE BLE DEVICE'S BATTERY LEVEL **/

    // To read the battery information form the Battery information service.
    static final UUID SERVICE_BATTERY_LEVEL = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    // Characteristic to read the battery status value.
    static final UUID CHAR_BATTERY_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    /**
     * For BLE Wheelchair Control
     */
    static final UUID SERVICE_WHEELCHAIR_CONTROL = UUID.fromString("00009923-1212-efde-1523-785feabcd123");
    static final UUID CHAR_WHEELCHAIR_CONTROL = UUID.fromString("00009925-1212-efde-1523-785feabcd123");

}
