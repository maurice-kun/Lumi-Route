package hs.fl.lumiroute.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R
import hs.fl.lumiroute.bluetooth.LumiApplication
import java.io.File

class TestFragment : Fragment() {

    private lateinit var directionArrow: ImageView
    private lateinit var distanceText: TextView

    // Bluetooth-Thread
    private val connectedThread = LumiApplication.getApplication().getCurrentConnectedThread()
    private var lastDirection: String = ""

    // Pfad zur Datei
    private val filePathOnAndroid: String
        get() = requireContext().getExternalFilesDir(null)?.absolutePath + "/navigation_data.txt"

    private val checkInterval = 1000L
    private var lastTimestamp: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("TestFragment", "onCreateView aufgerufen")
        val view = inflater.inflate(R.layout.fragment_test, container, false)

        directionArrow = view.findViewById(R.id.directionArrow)
        distanceText = view.findViewById(R.id.distanceText)

        if (hasManageExternalStoragePermission()) {
            Log.d("Permissions", "Berechtigungen bereits erteilt.")
            startFileMonitoring()
        } else {
            Log.d("Permissions", "Berechtigungen fehlen. Fordere an...")
            requestManageExternalStoragePermission()
        }

        val buttonBack = view.findViewById<Button>(R.id.btnBack)
        buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_testFragment_to_homeFragment)
        }

        return view
    }

    private fun startFileMonitoring() {
        Log.d("TestFragment", "Start File Monitoring...")
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                Log.d("FileMonitor", "Überprüfe Datei: $filePathOnAndroid")
                readLocalFile(filePathOnAndroid)
                handler.postDelayed(
                    this,
                    checkInterval
                )
            }
        }
        handler.post(runnable)
    }

    private fun readLocalFile(filePath: String) {
        Log.d("FileReader", "Versuche Datei zu lesen: $filePath")
        try {
            val file = File(filePath)
            if (file.exists()) {
                val content = file.readText()
                Log.d("FileReader", "Dateiinhalt: $content")

                val (timestamp, data) = parseFileContent(content)

                if (timestamp != lastTimestamp || data != lastDirection) {
                    lastTimestamp = timestamp
                    lastDirection = data
                    Log.d("FileReader", "Neue Daten erkannt: $data (Timestamp: $timestamp)")
                    processUnityData(data)
                } else {
                    Log.d(
                        "FileReader",
                        "Keine Änderungen erkannt. Timestamp: $timestamp, Richtung: $data"
                    )
                }
            } else {
                Log.e("FileReader", "Datei nicht gefunden: $filePath")
            }
        } catch (e: Exception) {
            Log.e("FileReader", "Fehler beim Lesen der Datei: ${e.message}")
        }
    }

    private fun processUnityData(data: String) {
        Log.d("UnityData", "Verarbeite Daten: $data")
        when {
            data.contains("A", ignoreCase = true) -> {
                directionArrow.setImageResource(R.drawable.ic_arrow_left)
                distanceText.text = "Links"
                sendSignalToArduino("l")
                Log.d("UnityData", "Richtung erkannt: Links (A)")
            }

            data.contains("D", ignoreCase = true) -> {
                directionArrow.setImageResource(R.drawable.ic_arrow_right)
                distanceText.text = "Rechts"
                sendSignalToArduino("r")
                Log.d("UnityData", "Richtung erkannt: Rechts (D)")
            }

            data.contains("W", ignoreCase = true) -> {
                directionArrow.setImageResource(R.drawable.ic_arrow_straight)
                distanceText.text = "Geradeaus"
                sendSignalToArduino("stopp")
                Log.d("UnityData", "Richtung erkannt: Geradeaus (W)")
            }

            data.contains("Stopp", ignoreCase = true) -> {
                directionArrow.setImageResource(R.drawable.logo_hs)
                distanceText.text = ""
                sendSignalToArduino("stopp")
                Log.d("UnityData", "Richtung erkannt: Stopp")
            }

            else -> {
                Log.d("UnityData", "Keine Richtung erkannt.")
            }
        }
    }

    private fun sendSignalToArduino(signal: String) {
        try {
            connectedThread?.write(signal)
            Log.d("Bluetooth", "Signal an Arduino gesendet: $signal")
        } catch (e: Exception) {
            Log.e("Bluetooth", "Fehler beim Senden des Signals: ${e.message}")
        }
    }

    private fun parseFileContent(content: String): Pair<String, String> {
        // Trennt den Timestamp und die Daten
        val parts = content.split("|")
        return if (parts.size == 2) {
            val dataParts = parts[1].split(":")
            val navigationHint = if (dataParts.size > 1) dataParts[1].trim() else ""
            parts[0] to navigationHint
        } else {
            "" to content
        }
    }


    private fun hasManageExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    private fun requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + requireContext().packageName)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(
                    "Permissions",
                    "Fehler beim Öffnen der Berechtigungseinstellungen: ${e.message}"
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasManageExternalStoragePermission()) {
            Log.d("Permissions", "Berechtigungen erteilt. Starte Dateiüberwachung.")
            startFileMonitoring()
        } else {
            Log.e("Permissions", "Berechtigungen weiterhin nicht erteilt.")
        }
    }
}
