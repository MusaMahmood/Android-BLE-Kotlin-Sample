package com.yeolabgt.mahmoodms.androidblekotlin

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast

import java.util.ArrayList

/**
 * Created by mahmoodms on 6/30/2016.
 * Main Activity Allows for scanning and selection of Bluetooth LE devices for data acquisition,
 * interfacing and classification.
 */

class MainActivity : Activity() {
    private var mScanning: Boolean = false
    private var mRunTraining = false
    private var mHandler: Handler? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var scannedDeviceAdapter: ScannedDeviceAdapter? = null
    private var selectedDeviceAdapter: ScannedDeviceAdapter? = null
    private val mDeviceAddressesMAC = ArrayList<String>()
    private val mDeviceNames = ArrayList<String>()
    private var mDevicesSelectedCount = 0

    private lateinit var mEditDelayText: EditText

    private val mScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            runOnUiThread {
                if (!mDeviceAddressesMAC.contains(result.device.address)) {
                    scannedDeviceAdapter!!.update(result.device, result.rssi, result.scanRecord)
                    scannedDeviceAdapter!!.notifyDataSetChanged()
                }
            }
            super.onScanResult(callbackType, result)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)
        //Set Orientation in Landscape
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        //Set up action bar:
        if (actionBar != null) actionBar!!.setDisplayHomeAsUpEnabled(false)
        val actionBar = actionBar
        actionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#800020")))
        //Set orientation of device based on screen type/size:
        //Flag to keep screen on (stay-awake):
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //Set up timer handler:
        mHandler = Handler()

        //Initialize scanningDeviceListView Adapter:
        val scanningDeviceListView = findViewById<ListView>(R.id.scanningList)
        val selectedDeviceListView = findViewById<ListView>(R.id.selectedList)
        //Check for BLE Support
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        }
        //Initialize Bluetooth manager
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val connectButton = findViewById<Button>(R.id.connectButton)
        //Initialize list view adapter:
        scannedDeviceAdapter = ScannedDeviceAdapter(this, R.layout.scanning_item, ArrayList())
        selectedDeviceAdapter = ScannedDeviceAdapter(this, R.layout.scanning_item, ArrayList())
        scanningDeviceListView.adapter = scannedDeviceAdapter
        selectedDeviceListView.adapter = selectedDeviceAdapter
        mEditDelayText = findViewById(R.id.editDelayText)
        // Click Item Listener:
        scanningDeviceListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val item = scannedDeviceAdapter!!.getItem(position)
            if (item != null) {
                if (!mDeviceAddressesMAC.contains(item.deviceMac)) {
                    mDeviceNames.add(item.displayName.toString())
                    mDeviceAddressesMAC.add(item.deviceMac)
                    mDevicesSelectedCount++
                    scannedDeviceAdapter!!.remove(position)
                    scannedDeviceAdapter!!.notifyDataSetChanged()
                    selectedDeviceAdapter!!.add(item)
                    selectedDeviceAdapter!!.notifyDataSetChanged()
                    Toast.makeText(this@MainActivity, "Device Selected: " + item.displayName + "\n" + item.deviceMac, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Device Already in List!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        connectButton.setOnClickListener {
            if (mDevicesSelectedCount > 0) {
                if (mScanning) {
                    if (mBluetoothAdapter!!.isEnabled)
                        mBluetoothAdapter!!.bluetoothLeScanner.stopScan(mScanCallback)
                    mScanning = false
                }
                //Send intent
                if (!mDeviceAddressesMAC.isEmpty()) {
                    val selectedDeviceArray = mDeviceAddressesMAC.toTypedArray()
                    val selectedDeviceNames = mDeviceNames.toTypedArray()
                    val selectedStimulus = arrayOfNulls<String>(1)
                    selectedStimulus[0] = mEditDelayText.text.toString()
                    val intent = Intent(this@MainActivity, DeviceControlActivity::class.java)
                    intent.putExtra(INTENT_DEVICES_KEY, selectedDeviceArray)
                    intent.putExtra(INTENT_DEVICES_NAMES, selectedDeviceNames)
                    intent.putExtra(INTENT_DELAY_VALUE_SECONDS, selectedStimulus)
                    intent.putExtra(INTENT_TRAIN_BOOLEAN, mRunTraining)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "No Devices Selected!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionCheck = intArrayOf(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION), ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE), ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS), ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE))
        val listPermissionsNeeded = ArrayList<String>()
        for (i in permissionCheck.indices) {
            if (permissionCheck[i] != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(PERMISSIONS_LIST[i])
            }
        }
        Log.e(TAG, "Permissions List Size: " + listPermissionsNeeded.size.toString())
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), MULTIPLE_PERMISSIONS_REQUEST)
            return false
        }
        return true
    }

    fun onCheckboxClicked(view: View) {
        val checked = (view as CheckBox).isChecked
        when (view.getId()) {
            R.id.trainingCheckbox -> mRunTraining = checked
        }
    }

    override fun onPause() {
        super.onPause()
        scanLeDevice(false)
        scannedDeviceAdapter!!.clear()
    }

    override fun onResume() {
        super.onResume()
        /*
         * Ensures Bluetooth is enabled on the device - if not enabled - fire intent to display a
         * dialog to ask permissioo enable
         */
        if (checkPermissions()) {
            if (!mBluetoothAdapter!!.isEnabled) {
                if (!mBluetoothAdapter!!.isEnabled) {
                    val enableBt = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBt, REQUEST_ENABLE_BT)
                }
            }
            scanLeDevice(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_device_scan, menu)
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).isVisible = false
            menu.findItem(R.id.menu_scan).isVisible = true
            menu.findItem(R.id.menu_refresh).actionView = null
        } else {
            menu.findItem(R.id.menu_stop).isVisible = true
            menu.findItem(R.id.menu_scan).isVisible = false
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_progress)
        }
        return true
    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            //stops scanning after ~seconds
            mHandler!!.postDelayed({
                mScanning = false
                if (mBluetoothAdapter!!.isEnabled)
                    mBluetoothAdapter!!.bluetoothLeScanner.stopScan(mScanCallback)
                invalidateOptionsMenu()
            }, SCAN_PERIOD)
            mScanning = true
            Log.i(TAG, "isNull" + (mBluetoothAdapter == null).toString())
            if (mBluetoothAdapter!!.isEnabled)
                mBluetoothAdapter!!.bluetoothLeScanner.startScan(mScanCallback)
        } else {
            mScanning = false
            if (mBluetoothAdapter!!.isEnabled)
                mBluetoothAdapter!!.bluetoothLeScanner.stopScan(mScanCallback)
        }
        invalidateOptionsMenu()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        //if user chose not to enable BT
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_scan -> {
                scannedDeviceAdapter!!.clear()
                scanLeDevice(true)
            }
            R.id.menu_stop -> scanLeDevice(false)
        }
        return true
    }

    companion object {
        private val TAG = DeviceControlActivity::class.java.simpleName
        private val MULTIPLE_PERMISSIONS_REQUEST = 139
        private val SCAN_PERIOD: Long = 10000
        private val REQUEST_ENABLE_BT = 12
        val INTENT_TRAIN_BOOLEAN = "BOOLEAN_TO_PARSE"
        val INTENT_DEVICES_KEY = "DEVICES_TO_PARSE"
        val INTENT_DEVICES_NAMES = "DEVICE_NAMES_TO_PARSE"
        val INTENT_DELAY_VALUE_SECONDS = "DELAY_VALUE_SECONDS"
        val PERMISSIONS_LIST = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE)
    }
}
