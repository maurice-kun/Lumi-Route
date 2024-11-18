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
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import hs.fl.lumiroute.R
import java.io.File

class TestFragment : Fragment() {

    private lateinit var directionArrow: ImageView
    private lateinit var distanceText: TextView

    // Pfad zur Datei im App-spezifischen Speicher
    private val filePathOnAndroid: String
        get() = requireContext().getExternalFilesDir(null)?.absolutePath + "/navigation_data.txt"

    private val checkInterval = 1000L // Intervall in Millisekunden (z. B. 1 Sekunde)
    private var lastTimestamp: String = "" // Speichert den zuletzt verarbeiteten Timestamp

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("TestFragment", "onCreateView aufgerufen")
        val view = inflater.inflate(R.layout.fragment_test, container, false)

        // Initialisiere UI-Elemente
        directionArrow = view.findViewById(R.id.directionArrow)
        distanceText = view.findViewById(R.id.distanceText)

        // Prüfe Berechtigungen
        if (hasManageExternalStoragePermission()) {
            Log.d("Permissions", "Berechtigungen bereits erteilt.")
            startFileMonitoring()
        } else {
            Log.d("Permissions", "Berechtigungen fehlen. Fordere an...")
            requestManageExternalStoragePermission()
        }

        return view
    }

    private fun startFileMonitoring() {
        Log.d("TestFragment", "Start File Monitoring...")
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                Log.d("FileMonitor", "Überprüfe Datei: $filePathOnAndroid")
                readLocalFile(filePathOnAndroid) // Datei lesen und verarbeiten
                handler.postDelayed(this, checkInterval) // Wiederholung alle `checkInterval` Millisekunden
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

                if (timestamp != lastTimestamp) {
                    lastTimestamp = timestamp
                    Log.d("FileReader", "Neue Daten erkannt: $data (Timestamp: $timestamp)")
                    processUnityData(data)
                } else {
                    Log.d("FileReader", "Keine Änderungen erkannt. Timestamp: $timestamp")
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

        // Daten direkt in der UI anzeigen
        distanceText.text = data

        // Beispielhafte Verarbeitung: Richtung anzeigen
        when {
            data.contains("A", ignoreCase = true) -> {
                directionArrow.setImageResource(R.drawable.ic_arrow_left)
                Log.d("UnityData", "Richtung erkannt: Links (A)")
            }
            data.contains("D", ignoreCase = true) -> {
                directionArrow.setImageResource(R.drawable.ic_arrow_right)
                Log.d("UnityData", "Richtung erkannt: Rechts (D)")
            }
            data.contains("W", ignoreCase = true) -> {
                directionArrow.setImageResource(R.drawable.ic_arrow_straight)
                Log.d("UnityData", "Richtung erkannt: Geradeaus (W)")
            }
            else -> {
                Log.d("UnityData", "Keine Richtung erkannt.")
            }
        }
    }


    private fun parseFileContent(content: String): Pair<String, String> {
        // Trennt den Timestamp und die Daten
        val parts = content.split("|")
        return if (parts.size == 2) {
            parts[0] to parts[1] // Timestamp und Daten
        } else {
            "" to content // Falls das Format nicht stimmt
        }
    }

    private fun hasManageExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true // Für ältere Android-Versionen
        }
    }

    private fun requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + requireContext().packageName)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("Permissions", "Fehler beim Öffnen der Berechtigungseinstellungen: ${e.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Überprüfe Berechtigung, wenn der Nutzer zurückkommt
        if (hasManageExternalStoragePermission()) {
            Log.d("Permissions", "Berechtigungen erteilt. Starte Dateiüberwachung.")
            startFileMonitoring()
        } else {
            Log.e("Permissions", "Berechtigungen weiterhin nicht erteilt.")
        }
    }
}
