package hs.fl.lumiroute.bluetooth;

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import hs.fl.lumiroute.R
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.UUID

class MainActivity : AppCompatActivity() {
    var arduinoBTModule: BluetoothDevice? = null
    var arduinoUUID: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") //We declare a default UUID to create the global variable
    var connectedThread: ConnectedThread? = null

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_connect)

        val bluetoothManager = getSystemService(
            BluetoothManager::class.java
        )
        val bluetoothAdapter = bluetoothManager.adapter
        //Intances of the Android UI elements that will will use during the execution of the APP
        val btDevices = findViewById<TextView>(R.id.textConnectHelmet)

        val connectToDevice = findViewById<View>(R.id.btnScanForDevices) as Button
        val seachDevices = findViewById<View>(R.id.btnPair) as Button
        val nextActivity = findViewById<Button>(R.id.btnBack)
        Log.d(TAG, "Begin Execution")

        //Using a handler to update the interface in case of an error connecting to the BT device
        //My idea is to show handler vs RxAndroid
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    ERROR_READ -> {
                        val arduinoMsg = msg.obj.toString() // Read message from Arduino

                        Toast.makeText(
                            this@MainActivity, arduinoMsg,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // Create an Observable from RxAndroid
        //The code will be executed when an Observer subscribes to the the Observable
        val connectToBTObservable =
            Observable.create { emitter: ObservableEmitter<ConnectedClass?> ->
                Log.d(
                    TAG,
                    "Calling connectThread class"
                )
                //Call the constructor of the ConnectThread class
                //Passing the Arguments: an Object that represents the BT device,
                // the UUID and then the handler to update the UI
                val connectThread = ConnectThread(
                    arduinoBTModule,
                    arduinoUUID,
                    handler
                )
                connectThread.run()
                //Check if Socket connected
                if (connectThread.mmSocket.isConnected) {
                    Log.d(
                        TAG,
                        "Calling ConnectedThread class"
                    )
                    //The pass the Open socket as arguments to call the constructor of ConnectedThread
                    connectedThread = ConnectedThread(connectThread.mmSocket)
                    if (connectedThread!!.mmInStream != null && connectedThread != null) {
                        val connected = ConnectedClass()
                        connected.isConnected = true
                        emitter.onNext(connected)
                        //MyApplication.setupConnectedThread();
                    }
                }
                emitter.onComplete()
            }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////// Find all Linked devices ///////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        seachDevices.setOnClickListener { //Check if the phone supports BT
            if (bluetoothAdapter == null) {
                // Device doesn't support Bluetooth
                Log.d(
                    TAG,
                    "Device doesn't support Bluetooth"
                )
            } else {
                Log.d(
                    TAG,
                    "Device support Bluetooth"
                )
                //Check BT enabled. If disabled, we ask the user to enable BT
                if (!bluetoothAdapter.isEnabled) {
                    Log.d(
                        TAG,
                        "Bluetooth is disabled"
                    )
                    val enableBtIntent =
                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Log.d(
                            TAG,
                            "We don't BT Permissions"
                        )
                        startActivityForResult(
                            enableBtIntent,
                            REQUEST_ENABLE_BT
                        )
                        Log.d(
                            TAG,
                            "Bluetooth is enabled now"
                        )
                    } else {
                        Log.d(
                            TAG,
                            "We have BT Permissions"
                        )
                        startActivityForResult(
                            enableBtIntent,
                            REQUEST_ENABLE_BT
                        )
                        Log.d(
                            TAG,
                            "Bluetooth is enabled now"
                        )
                    }
                } else {
                    Log.d(
                        TAG,
                        "Bluetooth is enabled"
                    )
                }
                var btDevicesString = ""
                val pairedDevices = bluetoothAdapter.bondedDevices

                if (pairedDevices.size > 0) {
                    // There are paired devices. Get the name and address of each paired device.
                    for (device in pairedDevices) {
                        val deviceName = device.name
                        val deviceHardwareAddress = device.address // MAC address
                        Log.d(
                            TAG,
                            "deviceName:$deviceName"
                        )
                        Log.d(
                            TAG,
                            "deviceHardwareAddress:$deviceHardwareAddress"
                        )
                        //We append all devices to a String that we will display in the UI
                        btDevicesString =
                            "$btDevicesString$deviceName || $deviceHardwareAddress\n"
                        //If we find the HC 05 device (the Arduino BT module)
                        //We assign the device value to the Global variable BluetoothDevice
                        //We enable the button "Connect to HC 05 device"
                        if (deviceName == "HC-05") {
                            Log.d(
                                TAG,
                                "HC-05 found"
                            )
                            arduinoUUID = device.uuids[0].uuid
                            arduinoBTModule = device
                            //HC -05 Found, enabling the button to read results
                            connectToDevice.isEnabled = true
                        }
                        btDevices.text = btDevicesString
                    }
                }
            }
            Log.d(
                TAG,
                "Button Pressed"
            )
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////// Call the observable to connect to the HC-05 ////////////////////////////////////////////
        ////////////////////////////////////////////// If it connects, the button to configure the LED will be enabled  ///////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        connectToDevice.setOnClickListener {
            if (arduinoBTModule != null) {
                connectToBTObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { connectedToBTDevice ->
                        if (connectedToBTDevice.isConnected) {
                            nextActivity.isEnabled = true
                        }
                    }
            }
        }


        //Next activity to configure the RGB LED
        nextActivity.setOnClickListener {
            MyApplication.getApplication().setupConnectedThread(connectedThread)
            val intent = Intent(
                this@MainActivity,
                ConfigureLed::class.java
            )
            startActivity(intent)
        }
    }

    companion object {
        // Global variables we will use in the
        private const val TAG = "FrugalLogs"
        private const val REQUEST_ENABLE_BT = 1

        //We will use a Handler to get the BT Connection statys
        var handler: Handler? = null
        private const val ERROR_READ = 0 // used in bluetooth handler to identify message update
    }
}
