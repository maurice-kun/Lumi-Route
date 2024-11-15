package hs.fl.lumiroute.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R
import hs.fl.lumiroute.bluetooth.ConnectedThread
import hs.fl.lumiroute.bluetooth.LumiApplication

class TestFragment : Fragment() {

    private var connectedThread: ConnectedThread? = null
    private lateinit var directionArrow: ImageView
    private lateinit var distanceText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)

        // Initialisiere UI-Elemente
        directionArrow = view.findViewById(R.id.directionArrow)
        distanceText = view.findViewById(R.id.distanceText)
        val backButton = view.findViewById<Button>(R.id.btnBack)

        // Bluetooth-Thread abrufen
        connectedThread = LumiApplication.getApplication().getCurrentConnectedThread()
        if (connectedThread == null) {
            distanceText.text = "Bluetooth-Verbindung nicht verfügbar"
        }

        // Back-Button zurück zur vorherigen Ansicht
        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_testFragment_to_settingsFragment)
        }

        // Starte die Simulation aller Richtungen
        simulateAllDirections()

        return view
    }

    private fun simulateAllDirections() {
        val directions = listOf(
            Pair("left", "100"),
            Pair("straight", "200"),
            Pair("right", "50")
        )

        Thread {
            directions.forEach { (type, distance) ->
                requireActivity().runOnUiThread {
                    processDirection(type, distance)
                }
                Thread.sleep(2000) // Zeige jede Richtung 2 Sekunden lang
            }
        }.start()
    }

    private fun processDirection(type: String, distance: String) {
        when (type) {
            "left" -> {
                directionArrow.setImageResource(R.drawable.ic_arrow_left)
                distanceText.text = "Entfernung: $distance m"
                sendSignalToArduino("L$distance")
            }

            "straight" -> {
                directionArrow.setImageResource(R.drawable.ic_arrow_straight)
                distanceText.text = "Entfernung: $distance m"
                sendSignalToArduino("S$distance")
            }

            "right" -> {
                directionArrow.setImageResource(R.drawable.ic_arrow_right)
                distanceText.text = "Entfernung: $distance m"
                sendSignalToArduino("R$distance")
            }
        }
    }

    private fun sendSignalToArduino(signal: String) {
        connectedThread?.write(signal)
            ?: distanceText.post { distanceText.text = "Bluetooth-Verbindung nicht verfügbar" }
    }
}
