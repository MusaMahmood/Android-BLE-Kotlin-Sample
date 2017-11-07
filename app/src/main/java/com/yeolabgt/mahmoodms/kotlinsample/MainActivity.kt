package com.yeolabgt.mahmoodms.kotlinsample

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_device_scan.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
//    private val mScanning = false
//    private val mRunTraining = false
//    private val mDeviceNames = arrayListOf<String>()
//    private val mDeviceAddresses = arrayListOf<String>()
//    private val mHandler = Handler()
//    val INTENT_TRAIN_BOOLEAN = "BOOLEAN_TO_PARSE"
//    val INTENT_DEVICES_KEY = "DEVICES_TO_PARSE"
//    val INTENT_DEVICES_NAMES = "DEVICE_NAMES_TO_PARSE"
//    val INTENT_DELAY_VALUE_SECONDS = "DELAY_VALUE_SECONDS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        if (actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
        }
        actionBar.setBackgroundDrawable(ColorDrawable(Color.parseColor("#804020")))
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

//        scanningList.adapter =

        // Example of a call to a native method
        Log.e(TAG, stringFromJNI())
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private external fun stringFromJNI(): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
