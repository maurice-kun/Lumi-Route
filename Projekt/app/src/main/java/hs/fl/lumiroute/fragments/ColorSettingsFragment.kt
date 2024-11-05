package hs.fl.lumiroute.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import hs.fl.lumiroute.R
import hs.fl.lumiroute.bluetooth.ConnectedThread
import hs.fl.lumiroute.bluetooth.LumiApplication

class ColorSettingsFragment : Fragment() {

    private lateinit var seekBarRed: SeekBar
    private lateinit var seekBarGreen: SeekBar
    private lateinit var seekBarBlue: SeekBar
    private lateinit var colorDemo: ImageView
    private var red = 0
    private var green = 0
    private var blue = 0

    private var connectedThread: ConnectedThread? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_colorsettings, container, false)

        seekBarRed = view.findViewById(R.id.seekBarRed)
        seekBarGreen = view.findViewById(R.id.seekBarGreen)
        seekBarBlue = view.findViewById(R.id.seekBarBlue)
        colorDemo = view.findViewById(R.id.colorDemo)

        // Bluetooth-Thread aus der Anwendung abrufen
        connectedThread = LumiApplication.getApplication().getCurrentConnectedThread()
        if (connectedThread == null) {
            Toast.makeText(context, "Bluetooth-Verbindung nicht verfügbar", Toast.LENGTH_SHORT).show()
        }

        // Listener für SeekBars
        seekBarRed.setOnSeekBarChangeListener(colorChangeListener)
        seekBarGreen.setOnSeekBarChangeListener(colorChangeListener)
        seekBarBlue.setOnSeekBarChangeListener(colorChangeListener)

        return view
    }

    private val colorChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            when (seekBar.id) {
                R.id.seekBarRed -> red = progress
                R.id.seekBarGreen -> green = progress
                R.id.seekBarBlue -> blue = progress
            }
            updateColor()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            sendColorToArduino()
        }
    }

    private fun updateColor() {
        colorDemo.setBackgroundColor(Color.rgb(red, green, blue))
    }

    private fun sendColorToArduino() {
        val colorData = "$red.$green.$blue"
        connectedThread?.write(colorData)
            ?: Toast.makeText(context, "Bluetooth-Verbindung nicht verfügbar", Toast.LENGTH_SHORT).show()
    }
}