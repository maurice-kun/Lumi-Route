package hs.fl.lumiroute.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import hs.fl.lumiroute.R
import hs.fl.lumiroute.bluetooth.ConnectedThread
import hs.fl.lumiroute.bluetooth.LumiApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class TestFragment : Fragment() {

    private lateinit var directionArrow: ImageView
    private lateinit var distanceText: TextView
    private var isConnected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)

        // Initialisiere UI-Elemente
        directionArrow = view.findViewById(R.id.directionArrow)
        distanceText = view.findViewById(R.id.distanceText)

        // Starte die Verbindung zum Unity-Server
        connectToUnityServer()

        return view
    }

    private fun connectToUnityServer() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val socket = Socket("192.168.0.100", 5050) // Unity-Server-IP und Port
                isConnected = true
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val writer = OutputStreamWriter(socket.getOutputStream())

                while (isConnected) {
                    val message = reader.readLine()
                    if (message != null) {
                        Log.d("TestFragment", "Empfangene Nachricht: $message")
                        requireActivity().runOnUiThread {
                            processUnityData(message)
                        }

                        // Optional: Antwort an Unity senden
                        writer.write("Daten empfangen\n")
                        writer.flush()
                    }
                }

                socket.close()
            } catch (e: Exception) {
                Log.e("TestFragment", "Fehler bei der Verbindung: ${e.message}")
            }
        }
    }

    private fun processUnityData(message: String) {
        try {
            // JSON-Daten parsen
            val jsonData = JSONObject(message)
            val positionX = jsonData.getDouble("positionX")
            val positionY = jsonData.getDouble("positionY")
            val positionZ = jsonData.getDouble("positionZ")

            // Zeige die Daten in der UI an
            distanceText.text = "X: $positionX, Y: $positionY, Z: $positionZ"

            // Interpretiere die Daten für Bewegungsanweisungen
            val direction = determineDirection(positionX, positionY, positionZ)
            updateUIForDirection(direction)

            // Sende die Richtung an das Arduino
            sendSignalToArduino(direction)
        } catch (e: Exception) {
            Log.e("TestFragment", "Fehler beim Verarbeiten der Nachricht: ${e.message}")
        }
    }

    private fun determineDirection(positionX: Double, positionY: Double, positionZ: Double): String {
        return when {
            positionX < -1 -> "left" // Links abbiegen
            positionX > 1 -> "right" // Rechts abbiegen
            else -> "straight" // Geradeaus
        }
    }

    private fun updateUIForDirection(direction: String) {
        when (direction) {
            "left" -> directionArrow.setImageResource(R.drawable.ic_arrow_left)
            "right" -> directionArrow.setImageResource(R.drawable.ic_arrow_right)
            "straight" -> directionArrow.setImageResource(R.drawable.ic_arrow_straight)
        }
    }

    private fun sendSignalToArduino(direction: String) {
        val signal = when (direction) {
            "left" -> "L100" // Beispiel: Links mit 100 m
            "right" -> "R100" // Beispiel: Rechts mit 100 m
            "straight" -> "S100" // Beispiel: Geradeaus mit 100 m
            else -> return
        }

        val connectedThread = LumiApplication.getApplication().getCurrentConnectedThread()
        connectedThread?.write(signal)
    }

    override fun onDestroy() {
        super.onDestroy()
        isConnected = false
    }
}
