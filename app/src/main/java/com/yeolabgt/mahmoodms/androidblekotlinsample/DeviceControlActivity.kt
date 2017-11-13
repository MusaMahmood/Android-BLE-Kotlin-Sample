package com.yeolabgt.mahmoodms.androidblekotlinsample

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.NavUtils
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton

import com.androidplot.util.Redrawer
//import com.beele.BluetoothLe
import com.google.common.primitives.Floats
import com.yeolabgt.mahmoodms.actblelibrary.ActBle

import org.tensorflow.contrib.android.TensorFlowInferenceInterface

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by mahmoodms on 5/31/2016.
 * Android Activity for Controlling Bluetooth LE Device Connectivity
 */

class DeviceControlActivity : Activity(), ActBle.ActBleListener {
    // Graphing Variables:
    private var mGraphInitializedBoolean = false
    private var mGraphAdapterCh1: GraphAdapter? = null
    private var mGraphAdapterCh2: GraphAdapter? = null
    private var mTimeDomainPlotAdapter: XYPlotAdapter? = null
    //Device Information
    private var mBleInitializedBoolean = false
    private lateinit var mBluetoothGattArray: Array<BluetoothGatt?>
    private var mActBle: ActBle? = null
    private var mDeviceName: String? = null
    private var mDeviceAddress: String? = null
    private var mConnected: Boolean = false
    private var mMSBFirst = false
    //Connecting to Multiple Devices
    private var deviceMacAddresses: Array<String>? = null
    private var mLedWheelchairControlService: BluetoothGattService? = null
    private var mWheelchairGattIndex: Int = 0
    private var mEEGConnectedAllChannels = false
    // Classification
    private var mNumber2ChPackets = -1
    private var mNumberOfClassifierCalls = 0
    private var mRunTrainingBool: Boolean = false
    //UI Elements - TextViews, Buttons, etc
    private var mTrainingInstructions: TextView? = null
    private var mBatteryLevel: TextView? = null
    private var mDataRate: TextView? = null
    private var mSSVEPClassTextView: TextView? = null
    private var mYfitTextView: TextView? = null
    private var mSButton: Button? = null
    private var mFButton: Button? = null
    private var mLButton: Button? = null
    private var mRButton: Button? = null
    private var mChannelSelect: ToggleButton? = null
    private var menu: Menu? = null
    //Data throughput counter
    private var mLastTime: Long = 0
    private var points = 0
    private val mTimerHandler = Handler()
    private var mTimerEnabled = false
    //Data Variables:
    private val batteryWarning = 20//
    private var dataRate: Double = 0.toDouble()
    private var mStimulusDelaySeconds = 0.0
    //Classification
    private var mWheelchairControl = false //Default classifier.
    //Play Sound:
    private lateinit var mMediaBeep: MediaPlayer
    //Tensorflow:
    private var mTFRunModel = false
    private var mTFInferenceInterface: TensorFlowInferenceInterface? = null
    private var mOutputScoresNames: Array<String>? = null

    private val timeStamp: String
        get() = SimpleDateFormat("yyyy.MM.dd_HH.mm.ss", Locale.US).format(Date())

    private val mClassifyThread = Runnable {
        val y: DoubleArray
        if (mTFRunModel) {
            //Run TF Model: SEE ORIGINAL .py SCRIPT TO VERIFY CORRECT INPUTS!
            val outputScores = FloatArray(5)//5 is number of classes/labels
            val ch1Doubles = FloatArray(WINDOW_DIMENSION_LENGTH_NORMAL)
            System.arraycopy(mCh1!!.classificationBufferFloats,
                    mCh1!!.classificationBufferSize - WINDOW_DIMENSION_LENGTH_NORMAL - 1,
                    ch1Doubles, 0, WINDOW_DIMENSION_LENGTH_NORMAL)
            val ch2Doubles = FloatArray(WINDOW_DIMENSION_LENGTH_NORMAL)
            System.arraycopy(mCh2!!.classificationBufferFloats,
                    mCh2!!.classificationBufferSize - WINDOW_DIMENSION_LENGTH_NORMAL - 1,
                    ch2Doubles, 0, WINDOW_DIMENSION_LENGTH_NORMAL)
            val mSSVEPDataFeedTF = Floats.concat(ch1Doubles, ch2Doubles)
            // 1 - feed probabilities:
            Log.i(TAG, "onCharacteristicChanged: TF_PRECALL_TIME, N#" + mNumberOfClassifierCalls.toString())
            mTFInferenceInterface!!.feed("keep_prob", floatArrayOf(1f))
            mTFInferenceInterface!!.feed(INPUT_DATA_FEED, mSSVEPDataFeedTF, WINDOW_DIMENSION_WIDTH, WINDOW_DIMENSION_LENGTH)
            mTFInferenceInterface!!.run(mOutputScoresNames)
            mTFInferenceInterface!!.fetch(OUTPUT_DATA_FEED, outputScores)
            Log.i(TAG, "[CALL#" + mNumberOfClassifierCalls.toString() + "]LABELS: " + Arrays.toString(LABELS))
            Log.d(TAG, "TF outputScores: " + Arrays.toString(outputScores))
            mNumberOfClassifierCalls++
        } else {
            Log.e(TAG, "[" + (mNumberOfClassifierCalls + 1).toString() + "] CALLING CLASSIFIER FUNCTION!")
            y = doubleArrayOf(-1.0, -1.0)
            mNumberOfClassifierCalls++
            Log.e(TAG, "Classifier Output: [#" + mNumberOfClassifierCalls.toString() + "::" + y[0].toString() + "," + y[1].toString() + "]")
            val s = "SSVEP cPSDA\n: [" + y[1].toString() + "]"
            runOnUiThread { mYfitTextView!!.text = s }
            executeWheelchairCommand(y[1].toInt())
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)
        //Set orientation of device based on screen type/size:
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        //Receive Intents:
        val intent = intent
        deviceMacAddresses = intent.getStringArrayExtra(MainActivity.INTENT_DEVICES_KEY)
        val deviceDisplayNames = intent.getStringArrayExtra(MainActivity.INTENT_DEVICES_NAMES)
        val intentStimulusClass = intent.getStringArrayExtra(MainActivity.INTENT_DELAY_VALUE_SECONDS)
        if (intent.extras != null)
            mRunTrainingBool = intent.extras!!.getBoolean(MainActivity.INTENT_TRAIN_BOOLEAN)
        else
            Log.e(TAG, "ERROR: intent.getExtras = null")

        mStimulusDelaySeconds = Integer.valueOf(intentStimulusClass[0])!!.toDouble()
        mDeviceName = deviceDisplayNames[0]
        mDeviceAddress = deviceMacAddresses!![0]
        Log.d(TAG, "Device Names: " + Arrays.toString(deviceDisplayNames))
        Log.d(TAG, "Device MAC Addresses: " + Arrays.toString(deviceMacAddresses))
        Log.d(TAG, Arrays.toString(deviceMacAddresses))
        //Set up action bar:
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        val actionBar = actionBar
        actionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#6078ef")))
        //Flag to keep screen on (stay-awake):
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //Set up TextViews
        val mExportButton = findViewById<Button>(R.id.button_export)
        mBatteryLevel = findViewById(R.id.batteryText)
        mTrainingInstructions = findViewById(R.id.trainingInstructions)
        updateTrainingView(mRunTrainingBool)
        mDataRate = findViewById(R.id.dataRate)
        mDataRate!!.text = "..."
        mYfitTextView = findViewById(R.id.textViewYfit)
        val ab = getActionBar()
        ab!!.title = mDeviceName
        ab.subtitle = mDeviceAddress
        //Initialize Bluetooth
        if (!mBleInitializedBoolean) initializeBluetoothArray()
        mMediaBeep = MediaPlayer.create(this, R.raw.beep_01a)
        mSButton = findViewById(R.id.buttonS)
        mFButton = findViewById(R.id.buttonF)
        mLButton = findViewById(R.id.buttonL)
        mRButton = findViewById(R.id.buttonR)
        val resetButton = findViewById<Button>(R.id.resetActivityButton)
        val mTensorflowSwitch = findViewById<Switch>(R.id.tensorflowClassificationSwitch)
        changeUIElementVisibility(false)
        mLastTime = System.currentTimeMillis()
        mSSVEPClassTextView = findViewById(R.id.eegClassTextView)
        // Initialize Tensorflow Inference Interface
        val customModelPath = Environment.getExternalStorageDirectory().absolutePath + "/Download/tensorflow_assets/opt_ssvep_net.pb"
        val customModel = File(customModelPath)
        val embeddedModel = File(MODEL_FILENAME)
        Log.d(TAG, "onCreate: customModel.exists: " + customModel.exists().toString())

        mOutputScoresNames = arrayOf(OUTPUT_DATA_FEED)

        //UI Listeners
        val toggleButton1 = findViewById<ToggleButton>(R.id.toggleButtonWheelchairControl)
        toggleButton1.setOnCheckedChangeListener { _, b ->
            mWheelchairControl = b
            executeWheelchairCommand(0)
            changeUIElementVisibility(b)
            mFilterData = b
        }
        mChannelSelect = findViewById(R.id.toggleButtonCh1)
        mChannelSelect!!.setOnCheckedChangeListener { _, b ->
            if (b) {
                mGraphAdapterCh2!!.clearPlot()
            } else {
                mGraphAdapterCh1!!.clearPlot()
            }
            mGraphAdapterCh1!!.plotData = b
            mGraphAdapterCh2!!.plotData = !b
        }
        mTensorflowSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                when {
                    customModel.exists() -> {
                        mTFInferenceInterface = TensorFlowInferenceInterface(assets, customModelPath)
                        //Reset counter:
                        mNumberOfClassifierCalls = 1
                        mTFRunModel = true
                        Log.i(TAG, "Tensorflow: customModel loaded")
                    }
                    embeddedModel.exists() -> { //Check if there's a model included:
                        compoundButton.isChecked = false
                        mTFRunModel = false
                        Toast.makeText(applicationContext, "No TF Model Found!", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        // No model found, continuing with original (reset switch)
                        compoundButton.isChecked = false
                        mTFRunModel = false
                        Toast.makeText(applicationContext, "No TF Model Found!", Toast.LENGTH_LONG).show()
                    }
                }
                if (mTFRunModel) {
                    Toast.makeText(applicationContext, "TF Model Loaded", Toast.LENGTH_SHORT).show()
                }
            } else {
                //Reset counter:
                mTFRunModel = false
                mNumberOfClassifierCalls = 1
                Toast.makeText(applicationContext, "Using PSD Analysis", Toast.LENGTH_SHORT).show()
            }
        }
        resetButton.setOnClickListener { resetActivity() }
        mSButton!!.setOnClickListener { executeWheelchairCommand(0) }
        mFButton!!.setOnClickListener { executeWheelchairCommand(1) }
        mLButton!!.setOnClickListener { executeWheelchairCommand(3) }
        mRButton!!.setOnClickListener { executeWheelchairCommand(2) }
        mExportButton.setOnClickListener { exportData() }
    }

    private fun resetActivity() {
        recreate()
    }

    private fun exportData() {
        try {
            terminateDataFileWriter()
        } catch (e: IOException) {
            Log.e(TAG, "IOException in saveDataFile")
            e.printStackTrace()
        }

        val context = applicationContext
        val uii = FileProvider.getUriForFile(context, context.packageName + ".provider", mPrimarySaveDataFile!!.file)
        val exportData = Intent(Intent.ACTION_SEND)
        exportData.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        exportData.putExtra(Intent.EXTRA_SUBJECT, "Sensor Data Export Details")
        exportData.putExtra(Intent.EXTRA_STREAM, uii)
        exportData.type = "text/html"
        startActivity(exportData)
    }

    @Throws(IOException::class)
    private fun terminateDataFileWriter() {
        mPrimarySaveDataFile?.terminateDataFileWriter()
    }

    public override fun onResume() {
        val a = DoubleArray(1000)
        Arrays.fill(a, 0.0)
        val b = DoubleArray(1000)
        Arrays.fill(b, 0.0)
        if (mRedrawer != null) {
            mRedrawer!!.start()
        }
        super.onResume()
    }

    override fun onPause() {
        if (mRedrawer != null) mRedrawer!!.pause()
        changeUIElementVisibility(false)
        super.onPause()
    }

    private fun initializeBluetoothArray() {
        val mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothDeviceArray = arrayOfNulls<BluetoothDevice>(deviceMacAddresses!!.size)
        Log.d(TAG, "Device Addresses: " + Arrays.toString(deviceMacAddresses))
        if (deviceMacAddresses != null) {
            for (i in deviceMacAddresses!!.indices) {
                mBluetoothDeviceArray[i] = mBluetoothManager.adapter.getRemoteDevice(deviceMacAddresses!![i])
            }
        } else {
            Log.e(TAG, "No Devices Queued, Restart!")
            Toast.makeText(this, "No Devices Queued, Restart!", Toast.LENGTH_SHORT).show()
        }
        mActBle = ActBle(this, mBluetoothManager, this)
        mBluetoothGattArray = Array(deviceMacAddresses!!.size, {i -> mActBle!!.connect(mBluetoothDeviceArray[i]) })
        for (i in mBluetoothDeviceArray.indices) {
            Log.e(TAG, "Connecting to Device: " + (mBluetoothDeviceArray[i]!!.name + " " + mBluetoothDeviceArray[i]!!.address))
            if ("WheelchairControl" == mBluetoothDeviceArray[i]!!.name) {
                mWheelchairGattIndex = i
                Log.e(TAG, "mWheelchairGattIndex: " + mWheelchairGattIndex)
                continue //we are done initializing
            }
            if ("EMG 250Hz" == mBluetoothDeviceArray[i]!!.name) {
                mMSBFirst = false
            } else if (mBluetoothDeviceArray[i]!!.name != null) {
                if (mBluetoothDeviceArray[i]!!.name.toLowerCase().contains("nrf52")) {
                    mMSBFirst = true
                }
            }
            val str = mBluetoothDeviceArray[i]!!.name.toLowerCase()
            when {
                str.contains("8k") -> {
                    mSampleRate = 8000
                }
                str.contains("4k") -> {
                    mSampleRate = 4000
                }
                str.contains("2k") -> {
                    mSampleRate = 2000
                }
                str.contains("1k") -> {
                    mSampleRate = 1000
                }
                str.contains("500") -> {
                    mSampleRate = 500
                }
                else -> {
                    mSampleRate = 250
                }
            }
            mPacketBuffer = mSampleRate/250
            Log.e(TAG, "mSampleRate: " + mSampleRate + "Hz")
            if (!mGraphInitializedBoolean) setupGraph()

            mGraphAdapterCh1!!.setxAxisIncrementFromSampleRate(mSampleRate)
            mGraphAdapterCh2!!.setxAxisIncrementFromSampleRate(mSampleRate)

            mGraphAdapterCh1!!.setSeriesHistoryDataPoints(250 * 5)
            mGraphAdapterCh2!!.setSeriesHistoryDataPoints(250 * 5)
            val fileNameTimeStamped = "EEG_SSVEPData_" + timeStamp + "_" + mSampleRate.toString() + "Hz"
            Log.e(TAG, "fileTimeStamp: " + fileNameTimeStamped)
            try {
                mPrimarySaveDataFile = SaveDataFile("/EEGData", fileNameTimeStamped,
                        24, 1.toDouble() / mSampleRate)
            } catch (e: IOException) {
                Log.e(TAG, "initializeBluetoothArray: IOException", e)
            }

        }
        mBleInitializedBoolean = true
    }

    private fun setupGraph() {
        // Initialize our XYPlot reference:
        mGraphAdapterCh1 = GraphAdapter(mSampleRate * 4, "EEG Data Ch 1", false, Color.BLUE) //Color.parseColor("#19B52C") also, RED, BLUE, etc.
        mGraphAdapterCh2 = GraphAdapter(mSampleRate * 4, "EEG Data Ch 2", false, Color.RED) //Color.parseColor("#19B52C") also, RED, BLUE, etc.
        mGraphAdapterCh1PSDA = GraphAdapter(mPSDDataPointsToShow, "EEG Power Spectrum (Ch1)", false, Color.BLUE)
        mGraphAdapterCh2PSDA = GraphAdapter(mPSDDataPointsToShow, "EEG Power Spectrum (Ch2)", false, Color.RED)
        //PLOT CH1 By default
        mGraphAdapterCh1!!.plotData = true
        mGraphAdapterCh1PSDA!!.plotData = true
        mGraphAdapterCh2PSDA!!.plotData = true
        mGraphAdapterCh1!!.setPointWidth(2.toFloat())
        mGraphAdapterCh2!!.setPointWidth(2.toFloat())
        mGraphAdapterCh1PSDA!!.setPointWidth(2.toFloat())
        mGraphAdapterCh2PSDA!!.setPointWidth(2.toFloat())
        mTimeDomainPlotAdapter = XYPlotAdapter(findViewById(R.id.eegTimeDomainXYPlot), false, 1000)
        if (mTimeDomainPlotAdapter!!.xyPlot != null) {
            mTimeDomainPlotAdapter!!.xyPlot!!.addSeries(mGraphAdapterCh1!!.series, mGraphAdapterCh1!!.lineAndPointFormatter)
            mTimeDomainPlotAdapter!!.xyPlot!!.addSeries(mGraphAdapterCh2!!.series, mGraphAdapterCh2!!.lineAndPointFormatter)
        }
        mFreqDomainPlotAdapter = XYPlotAdapter(findViewById(R.id.frequencyAnalysisXYPlot), "Frequency (Hz)", "Power Density (W/Hz)", mSampleRate.toDouble() / 125.0)
        if (mFreqDomainPlotAdapter!!.xyPlot != null) {
            mFreqDomainPlotAdapter!!.xyPlot!!.addSeries(mGraphAdapterCh1PSDA!!.series, mGraphAdapterCh1PSDA!!.lineAndPointFormatter)
            mFreqDomainPlotAdapter!!.xyPlot!!.addSeries(mGraphAdapterCh2PSDA!!.series, mGraphAdapterCh2PSDA!!.lineAndPointFormatter)
        }
        val xyPlotList = listOf(mTimeDomainPlotAdapter!!.xyPlot, mFreqDomainPlotAdapter!!.xyPlot)
        mRedrawer = Redrawer(xyPlotList, 30f, false)
        mRedrawer!!.start()
        mGraphInitializedBoolean = true
    }

    private fun setNameAddress(name_action: String?, address_action: String?) {
        val name = menu!!.findItem(R.id.action_title)
        val address = menu!!.findItem(R.id.action_address)
        name.title = name_action
        address.title = address_action
        invalidateOptionsMenu()
    }

    override fun onDestroy() {
        mRedrawer?.finish()
        disconnectAllBLE()
        try {
            terminateDataFileWriter()
        } catch (e: IOException) {
            Log.e(TAG, "IOException in saveDataFile")
            e.printStackTrace()
        }

        stopMonitoringRssiValue()
        super.onDestroy()
    }

    private fun disconnectAllBLE() {
        if (mActBle != null) {
            for (bluetoothGatt in mBluetoothGattArray) {
                mActBle!!.disconnect(bluetoothGatt!!)
                mConnected = false
                resetMenuBar()
            }
        }
    }

    private fun resetMenuBar() {
        runOnUiThread {
            if (menu != null) {
                menu!!.findItem(R.id.menu_connect).isVisible = true
                menu!!.findItem(R.id.menu_disconnect).isVisible = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_device_control, menu)
        menuInflater.inflate(R.menu.actionbar_item, menu)
        if (mConnected) {
            menu.findItem(R.id.menu_connect).isVisible = false
            menu.findItem(R.id.menu_disconnect).isVisible = true
        } else {
            menu.findItem(R.id.menu_connect).isVisible = true
            menu.findItem(R.id.menu_disconnect).isVisible = false
        }
        this.menu = menu
        setNameAddress(mDeviceName, mDeviceAddress)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_connect -> {
                if (mActBle != null) {
                    initializeBluetoothArray()
                }
                connect()
                return true
            }
            R.id.menu_disconnect -> {
                if (mActBle != null) {
                    disconnectAllBLE()
                }
                return true
            }
            android.R.id.home -> {
                if (mActBle != null) {
                    disconnectAllBLE()
                }
                NavUtils.navigateUpFromSameTask(this)
                onBackPressed()
                return true
            }
            R.id.action_settings -> {
                launchSettingsMenu()
                return true
            }
            R.id.action_export -> {
                exportData()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            val context = applicationContext
            //UI Stuff:
            val chSel = PreferencesFragment.channelSelect(context)
            val longPSDA = PreferencesFragment.psdaWideRange(context)
            val showPSDA = PreferencesFragment.showPSDA(context)
            val showUIElements = PreferencesFragment.showUIElements(context)
            //File Save Stuff
            val saveTimestamps = PreferencesFragment.saveTimestamps(context)
            val precision = (if (PreferencesFragment.setBitPrecision(context)) 64 else 32).toShort()
            val saveClass = PreferencesFragment.saveClass(context)
            mPrimarySaveDataFile!!.setSaveTimestamps(saveTimestamps)
            mPrimarySaveDataFile!!.setFpPrecision(precision)
            mPrimarySaveDataFile!!.setIncludeClass(saveClass)
            val filterData = PreferencesFragment.setFilterData(context)
            //TODO: for now just ch1:
            if (mGraphAdapterCh1 != null) {
                mFilterData = filterData
            }

            mGraphAdapterCh1PSDA!!.plotData = showPSDA
            mGraphAdapterCh2PSDA!!.plotData = showPSDA
            mFreqDomainPlotAdapter!!.setXyPlotVisibility(showPSDA)
            mFreqDomainPlotAdapter!!.xyPlot?.redraw()
            mTimeDomainPlotAdapter!!.xyPlot?.redraw()
            mChannelSelect!!.isChecked = chSel
            mGraphAdapterCh1!!.plotData = chSel
            mGraphAdapterCh2!!.plotData = !chSel
            if (longPSDA) {
                fPSDStartIndex = 0
                fPSDEndIndex = 249
            } else {
                fPSDStartIndex = 16
                fPSDEndIndex = 44
            }
            mWheelchairControl = showUIElements
            executeWheelchairCommand(0)
            changeUIElementVisibility(showUIElements)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun launchSettingsMenu() {
        val intent = Intent(applicationContext, SettingsActivity::class.java)
        startActivityForResult(intent, 1)
    }

    private fun connect() {
        runOnUiThread {
            val menuItem = menu!!.findItem(R.id.action_status)
            menuItem.title = "Connecting..."
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        Log.i(TAG, "onServicesDiscovered")
        if (status == BluetoothGatt.GATT_SUCCESS) {
            for (service in gatt.services) {
                if (service == null || service.uuid == null) {
                    continue
                }
                if (AppConstant.SERVICE_DEVICE_INFO == service.uuid) {
                    //Read the device serial number (if available)
                    if(service.getCharacteristic(AppConstant.CHAR_SERIAL_NUMBER) != null) {
                        mActBle!!.readCharacteristic(gatt, service.getCharacteristic(AppConstant.CHAR_SERIAL_NUMBER))
                    }
                    //Read the device software version (if available)
                    if(service.getCharacteristic(AppConstant.CHAR_SOFTWARE_REV) != null) {
                        mActBle!!.readCharacteristic(gatt, service.getCharacteristic(AppConstant.CHAR_SOFTWARE_REV))
                    }
                }
                if (AppConstant.SERVICE_WHEELCHAIR_CONTROL == service.uuid) {
                    mLedWheelchairControlService = service
                    Log.i(TAG, "BLE Wheelchair Control Service found")
                }

                if (AppConstant.SERVICE_EEG_SIGNAL == service.uuid) {
                    if (service.getCharacteristic(AppConstant.CHAR_EEG_CH1_SIGNAL) != null) {
                        mActBle!!.setCharacteristicNotifications(gatt, service.getCharacteristic(AppConstant.CHAR_EEG_CH1_SIGNAL), true)
                    }
                    if (service.getCharacteristic(AppConstant.CHAR_EEG_CH2_SIGNAL) != null) {
                        mActBle!!.setCharacteristicNotifications(gatt, service.getCharacteristic(AppConstant.CHAR_EEG_CH2_SIGNAL), true)
                    }
                    if (service.getCharacteristic(AppConstant.CHAR_EEG_CH3_SIGNAL) != null) {
                        mActBle!!.setCharacteristicNotifications(gatt, service.getCharacteristic(AppConstant.CHAR_EEG_CH3_SIGNAL), true)
                    }
                    if (service.getCharacteristic(AppConstant.CHAR_EEG_CH4_SIGNAL) != null) {
                        mActBle!!.setCharacteristicNotifications(gatt, service.getCharacteristic(AppConstant.CHAR_EEG_CH4_SIGNAL), true)
                    }
                }

                if (AppConstant.SERVICE_BATTERY_LEVEL == service.uuid) { //Read the device battery percentage
                    mActBle!!.readCharacteristic(gatt, service.getCharacteristic(AppConstant.CHAR_BATTERY_LEVEL))
                    mActBle!!.setCharacteristicNotifications(gatt, service.getCharacteristic(AppConstant.CHAR_BATTERY_LEVEL), true)
                }
            }
            //Run process only once:
            mActBle?.runProcess()
        }
    }

    private fun changeUIElementVisibility(visible: Boolean) {
        val viewVisibility = if (visible) View.VISIBLE else View.INVISIBLE
        runOnUiThread {
            mSButton!!.visibility = viewVisibility
            mFButton!!.visibility = viewVisibility
            mLButton!!.visibility = viewVisibility
            mRButton!!.visibility = viewVisibility
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        Log.i(TAG, "onCharacteristicRead")
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (AppConstant.CHAR_BATTERY_LEVEL == characteristic.uuid) {
                if(characteristic.value!=null) {
//                    val batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0)
                    val batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                    updateBatteryStatus(batteryLevel)
                    Log.i(TAG, "Battery Level :: " + batteryLevel)
                }
            }
        } else {
            Log.e(TAG, "onCharacteristic Read Error" + status)
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        if (mCh1 == null || mCh2 == null) {
            mCh1 = DataChannel(false, mMSBFirst, 4 * mSampleRate)
            mCh2 = DataChannel(false, mMSBFirst, 4 * mSampleRate)
        }

        if (AppConstant.CHAR_BATTERY_LEVEL == characteristic.uuid) {
            val batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)!!
            updateBatteryStatus(batteryLevel)
        }

        if (AppConstant.CHAR_EEG_CH1_SIGNAL == characteristic.uuid) {
            val mNewEEGdataBytes = characteristic.value
            if (!mCh1!!.chEnabled) {
                mCh1!!.chEnabled = true
            }
            getDataRateBytes(mNewEEGdataBytes.size)
            if (mEEGConnectedAllChannels) {
                mCh1!!.handleNewData(mNewEEGdataBytes)
                if (mCh1!!.packetCounter.toInt() == mPacketBuffer) {
                    addToGraphBuffer(mCh1!!, mGraphAdapterCh1, true)
                }
            }
        }

        if (AppConstant.CHAR_EEG_CH2_SIGNAL == characteristic.uuid) {
            if (!mCh2!!.chEnabled) {
                mCh2!!.chEnabled = true
            }
            val mNewEEGdataBytes = characteristic.value
            val byteLength = mNewEEGdataBytes.size
            getDataRateBytes(byteLength)
            if (mEEGConnectedAllChannels) {
                mCh2!!.handleNewData(mNewEEGdataBytes)
                if (mCh2!!.packetCounter.toInt() == mPacketBuffer) {
                    addToGraphBuffer(mCh2!!, mGraphAdapterCh2, false)
                }
            }
        }
        if (mCh1!!.chEnabled && mCh2!!.chEnabled) {
            mNumber2ChPackets++
            mEEGConnectedAllChannels = true
            mCh1!!.chEnabled = false
            mCh2!!.chEnabled = false
            if (mCh1!!.characteristicDataPacketBytes != null && mCh2!!.characteristicDataPacketBytes != null) {
                mPrimarySaveDataFile!!.writeToDisk(mCh1!!.characteristicDataPacketBytes, mCh2!!.characteristicDataPacketBytes)
            }
            if (mNumber2ChPackets % 10 == 0) { //Every x * 20 data points
                val classifyTaskThread = Thread(mClassifyThread)
                classifyTaskThread.start()
            }
            val powerSpectrumThreadTask = Thread(mPowerSpectrumRunnableThread)
            powerSpectrumThreadTask.start()
        }

        runOnUiThread {
            val concat = "C:[$mSSVEPClass]"
            mSSVEPClassTextView!!.text = concat
        }
    }

    private fun addToGraphBuffer(dataChannel: DataChannel, graphAdapter: GraphAdapter?, updateTrainingRoutine: Boolean) {
        if (mFilterData && dataChannel.totalDataPointsReceived > 1000) {
//            val filteredData = jSSVEPCfilter(dataChannel.classificationBuffer)
//            graphAdapter!!.clearPlot()
//
//            for (i in filteredData.indices) { // gA.addDataPointTimeDomain(y,x)
//                graphAdapter.addDataPointTimeDomainAlt(filteredData[i].toDouble(),
//                        dataChannel.totalDataPointsReceived - 999 + i)
//            }

        } else {
            if (dataChannel.dataBuffer != null) {
                if (mPrimarySaveDataFile!!.resolutionBits == 24) {
                    var i = 0
                    while (i < dataChannel.dataBuffer!!.size / 3) {
                        graphAdapter!!.addDataPointTimeDomain(DataChannel.bytesToDouble(dataChannel.dataBuffer!![3 * i],
                                dataChannel.dataBuffer!![3 * i + 1], dataChannel.dataBuffer!![3 * i + 2]),
                                dataChannel.totalDataPointsReceived - dataChannel.dataBuffer!!.size / 3 + i)
                        if (updateTrainingRoutine) {
                            for (j in 0 until graphAdapter.sampleRate / 250) {
                                updateTrainingRoutine(dataChannel.totalDataPointsReceived - dataChannel.dataBuffer!!.size / 3 + i + j)
                            }
                        }
                        i += graphAdapter.sampleRate / 250
                    }
                } else if (mPrimarySaveDataFile!!.resolutionBits == 16) {
                    var i = 0
                    while (i < dataChannel.dataBuffer!!.size / 2) {
                        graphAdapter!!.addDataPointTimeDomain(DataChannel.bytesToDouble(dataChannel.dataBuffer!![2 * i],
                                dataChannel.dataBuffer!![2 * i + 1]),
                                dataChannel.totalDataPointsReceived - dataChannel.dataBuffer!!.size / 2 + i)
                        if (updateTrainingRoutine) {
                            for (j in 0 until graphAdapter.sampleRate / 250) {
                                updateTrainingRoutine(dataChannel.totalDataPointsReceived - dataChannel.dataBuffer!!.size / 2 + i + j)
                            }
                        }
                        i += graphAdapter.sampleRate / 250
                    }
                }
            }
        }

        dataChannel.dataBuffer = null
        dataChannel.packetCounter = 0.toShort()
    }

    private val mPowerSpectrumRunnableThread = Runnable {
        runPowerSpectrum()
        powerSpectrumUpdateUI()
    }

    private fun updateTrainingRoutine(dataPoints: Int) {
        if (dataPoints % mSampleRate == 0 && mRunTrainingBool) {
            val second = dataPoints / mSampleRate
            val mSDS = mStimulusDelaySeconds.toInt()
            var eventSecondCountdown = 0
            when {
                (second in 0..mSDS) -> {
                    eventSecondCountdown = mSDS - second
                    updateTrainingPrompt("EYES CLOSED")
                    updateTrainingPromptColor(Color.GREEN)
                }
                (second in mSDS..(2*mSDS)) -> {
                    eventSecondCountdown = 2 * mSDS - second
                    updateTrainingPrompt("15.15Hz")
                    mSSVEPClass = 1.0
                }
                (second in (2*mSDS)..(3*mSDS)) -> {
                    eventSecondCountdown = 3 * mSDS - second
                    updateTrainingPrompt("16.67hz")
                    mSSVEPClass = 2.0
                }
                (second in (3*mSDS)..(4*mSDS)) -> {
                    eventSecondCountdown = 4 * mSDS - second
                    updateTrainingPrompt("18.51Hz")
                    mSSVEPClass = 3.0
                }
                (second in (4*mSDS)..(5*mSDS)) -> {
                    eventSecondCountdown = 5 * mSDS - second
                    updateTrainingPrompt("20.00Hz")
                    mSSVEPClass = 4.0
                }
                (second in (5*mSDS)..(6*mSDS)) -> {
                    eventSecondCountdown = 6 * mSDS - second
                    updateTrainingPrompt("Stop!")
                    updateTrainingPromptColor(Color.RED)
                    mSSVEPClass = 0.0
                    disconnectAllBLE()
                }
            }
            if (eventSecondCountdown == mSDS) {
                mMediaBeep.start()
            }
        }
    }

    private fun updateTrainingPrompt(prompt: String) {
        runOnUiThread {
            if (mRunTrainingBool) {
                mTrainingInstructions!!.text = prompt
            }
        }
    }

    private fun updateTrainingView(b: Boolean) {
        val visibility = if (b) View.VISIBLE else View.GONE
        runOnUiThread { mTrainingInstructions!!.visibility = visibility }
    }

    private fun updateTrainingPromptColor(color: Int) {
        runOnUiThread {
            if (mRunTrainingBool) {
                mTrainingInstructions!!.setTextColor(color)
            }
        }
    }

    private fun executeWheelchairCommand(command: Int) {
        val bytes = ByteArray(1)
        when (command) {
            0 -> bytes[0] = 0x00.toByte()
            1 -> bytes[0] = 0x01.toByte() //Stop
            2 -> bytes[0] = 0xF0.toByte() //?
            3 -> bytes[0] = 0x0F.toByte()
            4 // TODO: 6/27/2017 Disconnect instead of reverse?
            -> bytes[0] = 0xFF.toByte()
            else -> {
            }
        }
        if (mLedWheelchairControlService != null && mWheelchairControl) {
            Log.e(TAG, "SendingCommand: "+command.toString())
            Log.e(TAG, "SendingCommand (byte): "+bytes[0].toInt())
            mActBle!!.writeCharacteristic(mBluetoothGattArray[mWheelchairGattIndex]!!, mLedWheelchairControlService!!.getCharacteristic(AppConstant.CHAR_WHEELCHAIR_CONTROL), bytes)
        }
    }

    private fun getDataRateBytes(bytes: Int) {
        val mCurrentTime = System.currentTimeMillis()
        points += bytes
        if (mCurrentTime > mLastTime + 5000) {
            dataRate = (points / 5).toDouble()
            points = 0
            mLastTime = mCurrentTime
            Log.e(" DataRate:", dataRate.toString() + " Bytes/s")
            runOnUiThread {
                val s = dataRate.toString() + " Bytes/s"
                mDataRate!!.text = s
            }
        }
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
        uiRssiUpdate(rssi)
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                mConnected = true
                runOnUiThread {
                    if (menu != null) {
                        menu!!.findItem(R.id.menu_connect).isVisible = false
                        menu!!.findItem(R.id.menu_disconnect).isVisible = true
                    }
                }
                Log.i(TAG, "Connected")
                updateConnectionState(getString(R.string.connected))
                invalidateOptionsMenu()
                runOnUiThread {
                    mDataRate!!.setTextColor(Color.BLACK)
                    mDataRate!!.setTypeface(null, Typeface.NORMAL)
                }
                //Start the service discovery:
                gatt.discoverServices()
                startMonitoringRssiValue()
            }
            BluetoothProfile.STATE_CONNECTING -> {
            }
            BluetoothProfile.STATE_DISCONNECTING -> {
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                mConnected = false
                runOnUiThread {
                    if (menu != null) {
                        menu!!.findItem(R.id.menu_connect).isVisible = true
                        menu!!.findItem(R.id.menu_disconnect).isVisible = false
                    }
                }
                Log.i(TAG, "Disconnected")
                runOnUiThread {
                    mDataRate!!.setTextColor(Color.RED)
                    mDataRate!!.setTypeface(null, Typeface.BOLD)
                    mDataRate!!.text = HZ
                }
                updateConnectionState(getString(R.string.disconnected))
                stopMonitoringRssiValue()
                invalidateOptionsMenu()
            }
            else -> {
            }
        }
    }

    private fun startMonitoringRssiValue() {
        readPeriodicallyRssiValue(true)
    }

    private fun stopMonitoringRssiValue() {
        readPeriodicallyRssiValue(false)
    }

    private fun readPeriodicallyRssiValue(repeat: Boolean) {
        mTimerEnabled = repeat
        // check if we should stop checking RSSI value
        if (!mConnected || !mTimerEnabled) {
            mTimerEnabled = false
            return
        }

        mTimerHandler.postDelayed(Runnable {
            if (!mConnected) {
                mTimerEnabled = false
                return@Runnable
            }
            // request RSSI value
            mBluetoothGattArray[0]!!.readRemoteRssi()
            // add call it once more in the future
            readPeriodicallyRssiValue(mTimerEnabled)
        }, RSSI_UPDATE_TIME_INTERVAL.toLong())
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        Log.i(TAG, "onCharacteristicWrite :: Status:: " + status)
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {}

    override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        Log.i(TAG, "onDescriptorRead :: Status:: " + status)
    }

    override fun onError(errorMessage: String) {
        Log.e(TAG, "Error:: " + errorMessage)
    }

    private fun updateConnectionState(status: String) {
        runOnUiThread {
            if (status == getString(R.string.connected)) {
                Toast.makeText(applicationContext, "Device Connected!", Toast.LENGTH_SHORT).show()
            } else if (status == getString(R.string.disconnected)) {
                Toast.makeText(applicationContext, "Device Disconnected!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBatteryStatus(integerValue: Int) {
        val status: String
        val convertedBatteryVoltage = integerValue.toDouble() / 4096.0 * 7.20
        //Because TPS63001 dies below 1.8V, we need to set up a linear fit between 1.8-4.2V
        //Anything over 4.2V = 100%
        val finalPercent: Double = when {
            125.0 / 3.0 * convertedBatteryVoltage - 75.0 > 100.0 -> 100.0
            125.0 / 3.0 * convertedBatteryVoltage - 75.0 < 0.0 -> 0.0
            else -> 125.0 / 3.0 * convertedBatteryVoltage - 75.0
        }
        Log.e(TAG, "Battery Integer Value: " + integerValue.toString())
        Log.e(TAG, "ConvertedBatteryVoltage: " + String.format(Locale.US, "%.5f", convertedBatteryVoltage) + "V : " + String.format(Locale.US, "%.3f", finalPercent) + "%")
        status = String.format(Locale.US, "%.1f", finalPercent) + "%"
        runOnUiThread {
            if (finalPercent <= batteryWarning) {
                mBatteryLevel!!.setTextColor(Color.RED)
                mBatteryLevel!!.setTypeface(null, Typeface.BOLD)
                Toast.makeText(applicationContext, "Charge Battery, Battery Low " + status, Toast.LENGTH_SHORT).show()
            } else {
                mBatteryLevel!!.setTextColor(Color.GREEN)
                mBatteryLevel!!.setTypeface(null, Typeface.BOLD)
            }
            mBatteryLevel!!.text = status
        }
    }

    private fun uiRssiUpdate(rssi: Int) {
        runOnUiThread {
            val menuItem = menu!!.findItem(R.id.action_rssi)
            val statusActionItem = menu!!.findItem(R.id.action_status)
            val valueOfRSSI = rssi.toString() + " dB"
            menuItem.title = valueOfRSSI
            if (mConnected) {
                val newStatus = "Status: " + getString(R.string.connected)
                statusActionItem.title = newStatus
            } else {
                val newStatus = "Status: " + getString(R.string.disconnected)
                statusActionItem.title = newStatus
            }
        }
    }

    private fun runPowerSpectrum() {
//        val combinedPSDArray: DoubleArray
//        mPSDCh1 = DoubleArray(mSampleRate)
//        mPSDCh2 = DoubleArray(mSampleRate)
//        val getInstancePSD1 = DoubleArray(mSampleRate * 2)
//        val getInstancePSD2 = DoubleArray(mSampleRate * 2)
//        System.arraycopy(mCh1!!.classificationBuffer, mSampleRate * 2, getInstancePSD1, 0, mSampleRate * 2)
//        System.arraycopy(mCh2!!.classificationBuffer, mSampleRate * 2, getInstancePSD2, 0, mSampleRate * 2)
//        if (mSampleRate < 8000) {
//        } else {
//            Arrays.fill(mPSDCh1!!, 0.0)
//            Arrays.fill(mPSDCh2!!, 0.0)
//        }
    }

    private fun powerSpectrumUpdateUI() {
        mPSDDataPointsToShow = fPSDEndIndex - fPSDStartIndex
        mGraphAdapterCh1PSDA!!.setSeriesHistoryDataPoints(mPSDDataPointsToShow)
        mGraphAdapterCh2PSDA!!.setSeriesHistoryDataPoints(mPSDDataPointsToShow)
        if (mPSDDataPointsToShow > 64)
            mFreqDomainPlotAdapter!!.setXyPlotDomainIncrement(6.0)
        else
            mFreqDomainPlotAdapter!!.setXyPlotDomainIncrement(2.0)
//        mGraphAdapterCh1PSDA!!.addDataPointsGeneric(fPSD!!, mPSDCh1!!, fPSDStartIndex, fPSDEndIndex)
//        mGraphAdapterCh2PSDA!!.addDataPointsGeneric(fPSD!!, mPSDCh2!!, fPSDStartIndex, fPSDEndIndex)
    }

    companion object {
        val HZ = "0 Hz"
        private val TAG = DeviceControlActivity::class.java.simpleName
        private var mGraphAdapterCh1PSDA: GraphAdapter? = null
        private var mGraphAdapterCh2PSDA: GraphAdapter? = null
        private var mFreqDomainPlotAdapter: XYPlotAdapter? = null
        var mRedrawer: Redrawer? = null
        // Power Spectrum Graph Data:
        private var mPSDDataPointsToShow = 0
        internal var fPSDStartIndex = 16
        internal var fPSDEndIndex = 44
        private var mSampleRate = 250
        //Data Channel Classes
        internal var mCh1: DataChannel? = null
        internal var mCh2: DataChannel? = null
        internal var mFilterData = false
        private var mPacketBuffer = 6
        //RSSI:
        private val RSSI_UPDATE_TIME_INTERVAL = 2000
        var mSSVEPClass = 0.0
        //Save Data File
        private var mPrimarySaveDataFile: SaveDataFile? = null
        //Tensorflow CONSTANTS:
        val INPUT_DATA_FEED = "input"
        val OUTPUT_DATA_FEED = "output"
        val WINDOW_DIMENSION_LENGTH_NORMAL = 300
        val WINDOW_DIMENSION_LENGTH = WINDOW_DIMENSION_LENGTH_NORMAL * 2L
        val WINDOW_DIMENSION_WIDTH = 1L
        private val LABELS = arrayOf("Alpha", "15.15Hz", "16.67Hz", "18.51Hz", "20.00Hz")
        //Directory:
        private val MODEL_FILENAME = "file:///android_asset/opt_ssvep_net.pb"
        //Note for companion object: JNI call must include Companion in call: e.g. package_class_Companion_function(...).
        //TODO: Still does not work when I try to call from the companion object.
    }
}
