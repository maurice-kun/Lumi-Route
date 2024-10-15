package hs.fl.lumiroute.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R
import hs.fl.lumiroute.bluetooth.ConnectedClass
import hs.fl.lumiroute.bluetooth.ConnectedThread
import hs.fl.lumiroute.bluetooth.ConnectThread
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class ConnectFragment : Fragment() {
    // Bluetooth variables
    private var arduinoBTModule: BluetoothDevice? = null
    private var arduinoUUID: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Default UUID
    private var connectedThread: ConnectedThread? = null

    // Handler for Bluetooth communication
    private var handler: Handler? = null

    // UI elements
    private lateinit var btDevices: TextView
    private lateinit var connectToDevice: Button
    private lateinit var searchDevices: Button
    private lateinit var backButton: Button

    // Constants
    companion object {
        private const val TAG = "FrugalLogs"
        private const val REQUEST_ENABLE_BT = 1
        private const val ERROR_READ = 0 // used in bluetooth handler to identify message update
    }

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connect, container, false)

        // Initialize UI elements
        btDevices = view.findViewById(R.id.textConnectHelmet)
        connectToDevice = view.findViewById(R.id.btnPair)
        searchDevices = view.findViewById(R.id.btnScanForDevices)
        backButton = view.findViewById(R.id.btnBack)

        // Initialize Bluetooth manager and adapter
        val bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        Log.d(TAG, "Begin Execution")

        // Handler to update UI in case of an error connecting to the BT device
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    ERROR_READ -> {
                        val arduinoMsg = msg.obj.toString() // Read message from Arduino

                        Toast.makeText(
                            requireContext(), arduinoMsg,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // Create an Observable for Bluetooth connection
        val connectToBTObservable =
            Observable.create { emitter: ObservableEmitter<ConnectedClass?> ->
                Log.d(
                    TAG,
                    "Calling connectThread class"
                )
                val connectThread = ConnectThread(
                    arduinoBTModule,
                    arduinoUUID,
                    handler
                )
                connectThread.run()
                // Check if Socket connected
                if (connectThread.mmSocket.isConnected) {
                    Log.d(
                        TAG,
                        "Calling ConnectedThread class"
                    )
                    connectedThread = ConnectedThread(connectThread.mmSocket)
                    if (connectedThread != null && connectedThread!!.mmInStream != null) {
                        val connected = ConnectedClass()
                        connected.isConnected = true
                        emitter.onNext(connected)
                        // MyApplication.setupConnectedThread();
                    }
                }
                emitter.onComplete()
            }

        //////////////////////////////////////////////////////////////////////////////////////////////////////
        // Find all Paired devices
        //////////////////////////////////////////////////////////////////////////////////////////////////////
        searchDevices.setOnClickListener {
            // Check if the phone supports BT
            if (bluetoothAdapter == null) {
                // Device doesn't support Bluetooth
                Log.d(TAG, "Device doesn't support Bluetooth")
                Toast.makeText(
                    requireContext(),
                    "Device doesn't support Bluetooth",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Log.d(TAG, "Device supports Bluetooth")
                // Check if Bluetooth is enabled
                if (!bluetoothAdapter.isEnabled) {
                    Log.d(TAG, "Bluetooth is disabled")
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBluetoothLauncher.launch(enableBtIntent)
                } else {
                    Log.d(TAG, "Bluetooth is enabled")
                    // Proceed to check for permissions
                    checkBluetoothPermissionsAndListDevices(bluetoothAdapter)
                }
            }
            Log.d(TAG, "Search Devices Button Pressed")
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////
        // Call the observable to connect to the HC-05
        // If it connects, the button to configure the LED will be enabled
        //////////////////////////////////////////////////////////////////////////////////////////////////////
        connectToDevice.setOnClickListener {
            if (arduinoBTModule != null) {
                connectToBTObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { connectedToBTDevice ->
                            if (connectedToBTDevice?.isConnected == true) {
                                // Connection successful
                                Toast.makeText(
                                    requireContext(),
                                    "Connected to HC-05",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        { throwable ->
                            // Handle error
                            Log.e(TAG, "Error connecting to Bluetooth device", throwable)
                            Toast.makeText(
                                requireContext(),
                                "Error connecting to Bluetooth device",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
            }
        }

        // Back button
        backButton.setOnClickListener {
            // Navigate back to the home fragment
            findNavController().navigate(R.id.action_connectFragment_to_homeFragment)
        }

        return view
    }

    // Launcher to enable Bluetooth
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Bluetooth enabled
            Log.d(TAG, "Bluetooth is enabled now")
            // Proceed to check for permissions
            val bluetoothManager =
                requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            checkBluetoothPermissionsAndListDevices(bluetoothAdapter)
        } else {
            // User denied to enable Bluetooth
            Log.d(TAG, "Bluetooth is not enabled")
            Toast.makeText(requireContext(), "Bluetooth is not enabled", Toast.LENGTH_LONG).show()
        }
    }

    // Function to check permissions and list paired devices
    private fun checkBluetoothPermissionsAndListDevices(bluetoothAdapter: BluetoothAdapter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 and above
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request Bluetooth permissions
                requestPermissions(
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_ENABLE_BT
                )
                Log.d(TAG, "Requested Bluetooth permissions")
            } else {
                // Permissions granted, proceed to list devices
                listPairedDevices(bluetoothAdapter)
            }
        } else {
            // Below Android 12
            listPairedDevices(bluetoothAdapter)
        }
    }

    // Function to list paired devices
    private fun listPairedDevices(bluetoothAdapter: BluetoothAdapter) {
        var btDevicesString = ""
        val pairedDevices = bluetoothAdapter.bondedDevices

        if (pairedDevices.isNotEmpty()) {
            // There are paired devices. Get the name and address of each paired device.
            for (device in pairedDevices) {
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                Log.d(TAG, "deviceName: $deviceName")
                Log.d(TAG, "deviceHardwareAddress: $deviceHardwareAddress")
                // Append all devices to a String to display in the UI
                btDevicesString += "$deviceName || $deviceHardwareAddress\n"
                // If we find the HC-05 device (the Arduino BT module)
                // We assign the device value to the Global variable BluetoothDevice
                if (deviceName == "HC-05") {
                    Log.d(TAG, "HC-05 found")
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // Request the missing permissions and handle the result
                        requestPermissions(
                            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                            REQUEST_ENABLE_BT
                        )
                        return
                    }
                    // Now safe to access device.uuids
                    val deviceUuids = device.uuids
                    if (deviceUuids != null && deviceUuids.isNotEmpty()) {
                        arduinoUUID = deviceUuids[0].uuid
                    } else {
                        // Handle the case where device.uuids is null or empty
                        arduinoUUID =
                            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                    }
                    arduinoBTModule = device
                    // HC-05 Found, enabling the button to connect
                    connectToDevice.isEnabled = true
                }
            }
            btDevices.text = btDevicesString
        } else {
            btDevices.text = "No paired devices found."
            Toast.makeText(requireContext(), "No paired devices found.", Toast.LENGTH_LONG).show()
        }
    }

    // Handle the result of permission requests
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d(TAG, "Bluetooth permissions granted")
                // Proceed to list paired devices
                val bluetoothManager =
                    requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val bluetoothAdapter = bluetoothManager.adapter
                listPairedDevices(bluetoothAdapter)
            } else {
                // Permission denied
                Log.d(TAG, "Bluetooth permissions denied")
                Toast.makeText(
                    requireContext(),
                    "Bluetooth permissions are required to list paired devices.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
