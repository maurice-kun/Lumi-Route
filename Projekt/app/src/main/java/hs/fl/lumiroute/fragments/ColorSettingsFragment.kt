package hs.fl.lumiroute.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R

class ColorSettingsFragment : Fragment() {

    // Variablen für die SeekBars und TextViews
    private lateinit var seekBarRed: SeekBar
    private lateinit var seekBarGreen: SeekBar
    private lateinit var seekBarBlue: SeekBar

    private lateinit var redValueText: TextView
    private lateinit var greenValueText: TextView
    private lateinit var blueValueText: TextView

    private lateinit var colorDemo: ImageView

    // Farbwerte
    private var red = 0
    private var green = 0
    private var blue = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_colorsettings, container, false)

        // Initialisierung der Views
        seekBarRed = view.findViewById(R.id.seekBarRed)
        seekBarGreen = view.findViewById(R.id.seekBarGreen)
        seekBarBlue = view.findViewById(R.id.seekBarBlue)

        redValueText = view.findViewById(R.id.redValue)
        greenValueText = view.findViewById(R.id.greenValue)
        blueValueText = view.findViewById(R.id.blueValue)

        colorDemo = view.findViewById(R.id.colorDemo)

        // Listener für die SeekBars hinzufügen
        seekBarRed.setOnSeekBarChangeListener(colorChangeListener)
        seekBarGreen.setOnSeekBarChangeListener(colorChangeListener)
        seekBarBlue.setOnSeekBarChangeListener(colorChangeListener)

        // Anfangswerte der TextViews setzen
        redValueText.text = "Red: $red"
        greenValueText.text = "Green: $green"
        blueValueText.text = "Blue: $blue"

        // Initiale Farbvorschau aktualisieren
        updateColor()

        // Back-Button einrichten
        val buttonBack = view.findViewById<Button>(R.id.btnBack)
        buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_colorSettingsFragment_to_settingsFragment)
        }

        return view
    }

    // Listener für die SeekBars
    private val colorChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            when (seekBar.id) {
                R.id.seekBarRed -> {
                    red = progress
                    redValueText.text = "Red: $red"
                }

                R.id.seekBarGreen -> {
                    green = progress
                    greenValueText.text = "Green: $green"
                }

                R.id.seekBarBlue -> {
                    blue = progress
                    blueValueText.text = "Blue: $blue"
                }
            }
            // Farbvorschau aktualisieren
            updateColor()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // Nicht benötigt
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            // Nicht benötigt
        }
    }

    // Methode zur Aktualisierung der Farbvorschau
    private fun updateColor() {
        val color = Color.rgb(red, green, blue)
        colorDemo.setBackgroundColor(color)
    }
}
