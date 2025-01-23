package hs.fl.lumiroute.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R
import hs.fl.lumiroute.bluetooth.ConnectedThread
import hs.fl.lumiroute.bluetooth.LumiApplication

class DebugFragment : Fragment() {

    private var connectedThread: ConnectedThread? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_debug, container, false)

        // Bluetooth-Thread aus der Anwendung abrufen
        connectedThread = LumiApplication.getApplication().getCurrentConnectedThread()
        if (connectedThread == null) {
            Toast.makeText(context, "Bluetooth-Verbindung nicht verfügbar", Toast.LENGTH_SHORT)
                .show()
        }

        val buttonLeft = view.findViewById<Button>(R.id.btnBlinkLeft)
        buttonLeft.setOnClickListener {
            sendSignalToArduino("l")
        }

        val buttonRight = view.findViewById<Button>(R.id.btnBlinkRight)
        buttonRight.setOnClickListener {
            sendSignalToArduino("r")
        }

        val buttonWarn = view.findViewById<Button>(R.id.btnWarningLights)
        buttonWarn.setOnClickListener {
            sendSignalToArduino("w")
        }

        val buttonDisconnect = view.findViewById<Button>(R.id.btnDisconnect)
        buttonDisconnect.setOnClickListener {
            sendSignalToArduino("d")
        }

        val buttonStop = view.findViewById<Button>(R.id.btnStop)
        buttonStop.setOnClickListener {
            sendSignalToArduino("stop")
        }

        val buttonBack = view.findViewById<Button>(R.id.btnBack)
        buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_debugFragment_to_settingsFragment)
        }

        return view
    }

    // Funktion zum Senden eines Signals an den Arduino
    private fun sendSignalToArduino(signal: String) {
        connectedThread?.write(signal)
            ?: Toast.makeText(context, "Bluetooth-Verbindung nicht verfügbar", Toast.LENGTH_SHORT)
                .show()
    }
}
