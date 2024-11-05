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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R
import hs.fl.lumiroute.bluetooth.ConnectedClass
import hs.fl.lumiroute.bluetooth.ConnectedThread
import hs.fl.lumiroute.bluetooth.ConnectThread
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
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
    private lateinit var deviceListView: ListView
    private lateinit var connectToDeviceButton: Button
    private lateinit var searchDevicesButton: Button
    private lateinit var backButton: Button
    private val devices = ArrayList<BluetoothDevice>()
    private val deviceList = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    // Constants
    companion object {
        private const val TAG = "ConnectFragment"
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_PERMISSIONS = 2
        private const val ERROR_READ = 0 // used in Bluetooth handler to identify message update
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connect, container, false)

        // Initialize UI elements
        deviceListView = view.findViewById(R.id.deviceListView)
        connectToDeviceButton = view.findViewById(R.id.btnPair)
        searchDevicesButton = view.findViewById(R.id.btnScanForDevices)
        backButton = view.findViewById(R.id.btnBack)

        // Initialize Bluetooth manager and adapter
        val bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        Log.d(TAG, "Begin Execution")

        // Set up the list adapter
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, deviceList)
        deviceListView.adapter = adapter
        deviceListView.visibility = View.GONE
        connectToDeviceButton.visibility = View.GONE

        // Handler to update UI in case of an error connecting to the BT device
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    ERROR_READ -> {
                        val arduinoMsg = msg.obj.toString() // Read message from Arduino
                        Toast.makeText(requireContext(), arduinoMsg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Create an Observable for Bluetooth connection
        val connectToBTObservable = Observable.create<ConnectedClass> { emitter ->
            Log.d(TAG, "Calling connectThread class")
            val connectThread = ConnectThread(arduinoBTModule, arduinoUUID, handler)
            connectThread.run()

            if (connectThread.mmSocket.isConnected) {
                Log.d(TAG, "Calling ConnectedThread class")
                connectedThread = ConnectedThread(connectThread.mmSocket)
                if (connectedThread != null && connectedThread!!.mmInStream != null) {
                    val connected = ConnectedClass().apply {
                        isConnected = true
                    }
                    emitter.onNext(connected)
                }
            } else {
                Log.e(TAG, "Failed to connect to Bluetooth device")
                emitter.onError(Throwable("Failed to connect to Bluetooth device"))
            }
            emitter.onComplete()
        }


        // Set up button listeners
        searchDevicesButton.setOnClickListener {
            if (bluetoothAdapter == null) {
                Toast.makeText(requireContext(), "Bluetooth is not available", Toast.LENGTH_LONG)
                    .show()
                Log.e(TAG, "Bluetooth is not available when search button clicked.")
            } else {
                Log.d(TAG, "Search button clicked, checking permissions...")
                checkPermissionsAndListDevices(bluetoothAdapter)
            }
        }

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            arduinoBTModule = devices[position]
            Toast.makeText(
                requireContext(),
                "Selected: ${arduinoBTModule?.name}",
                Toast.LENGTH_SHORT
            ).show()
            Log.d(TAG, "Device selected: ${arduinoBTModule?.name}")
            connectToDeviceButton.visibility = View.VISIBLE
        }

        connectToDeviceButton.setOnClickListener {
            if (arduinoBTModule != null) {
                connectToBTObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { connectedToBTDevice ->
                            if (connectedToBTDevice?.isConnected == true) {
                                Toast.makeText(
                                    requireContext(),
                                    "Connected to ${arduinoBTModule?.name}",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.d(TAG, "Successfully connected to ${arduinoBTModule?.name}")
                            }
                        },
                        { throwable ->
                            Log.e(TAG, "Error connecting to Bluetooth device", throwable)
                            Toast.makeText(
                                requireContext(),
                                "Error connecting to Bluetooth device",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
            } else {
                Log.w(TAG, "Connect button clicked but no device selected.")
                Toast.makeText(requireContext(), "No device selected", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_connectFragment_to_homeFragment)
        }

        return view
    }

    // Launcher to enable Bluetooth
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            Log.d(TAG, "Bluetooth is enabled now")
            val bluetoothManager =
                requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            checkPermissionsAndListDevices(bluetoothAdapter)
        } else {
            Log.d(TAG, "Bluetooth is not enabled")
            Toast.makeText(requireContext(), "Bluetooth is not enabled", Toast.LENGTH_LONG).show()
        }
    }

    // Function to check permissions and list paired devices
    private fun checkPermissionsAndListDevices(bluetoothAdapter: BluetoothAdapter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_ENABLE_BT
                )
                Log.d(TAG, "Requested Bluetooth permissions")
            } else {
                listPairedDevices(bluetoothAdapter)
            }
        } else {
            listPairedDevices(bluetoothAdapter)
        }
    }

    // Function to list paired devices
    @SuppressLint("MissingPermission")
    private fun listPairedDevices(bluetoothAdapter: BluetoothAdapter) {
        Log.d(TAG, "Listing paired devices...")
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Bluetooth permissions not granted to list paired devices.")
            Toast.makeText(
                requireContext(),
                "Bluetooth permissions not granted.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices

        if (pairedDevices.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No paired devices found.", Toast.LENGTH_LONG).show()
            Log.w(TAG, "No paired devices found.")
        } else {
            deviceList.clear()
            devices.clear()
            pairedDevices.forEach { device ->
                devices.add(device)
                deviceList.add("${device.name} (${device.address})")
                Log.d(TAG, "Found paired device: ${device.name} (${device.address})")
            }
            adapter.notifyDataSetChanged()
            deviceListView.visibility = View.VISIBLE
            Log.d(TAG, "Paired devices displayed in list.")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ENABLE_BT || requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d(TAG, "Permissions granted, proceeding to list paired devices...")
                val bluetoothManager =
                    requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val bluetoothAdapter = bluetoothManager.adapter
                listPairedDevices(bluetoothAdapter)
            } else {
                Log.w(TAG, "Bluetooth permissions were denied.")
                Toast.makeText(
                    requireContext(),
                    "Bluetooth permissions are required to list paired devices.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
