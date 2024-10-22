package hs.fl.lumiroute.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R

class LightSettingsFragment : Fragment() {

    private lateinit var spinnerLightIntensity: Spinner
    private lateinit var spinnerLightFrequency: Spinner
    private lateinit var btnBack: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lightsettings, container, false)

        spinnerLightIntensity = view.findViewById(R.id.spinnerLightIntensity)
        spinnerLightFrequency = view.findViewById(R.id.spinnerLightFrequency)
        btnBack = view.findViewById(R.id.btnBack)

        spinnerLightIntensity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View,
                position: Int, id: Long
            ) {
                val selectedIntensity = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        spinnerLightFrequency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View,
                position: Int, id: Long
            ) {
                val selectedFrequency = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_lightSettingsFragment_to_settingsFragment)
        }

        return view
    }

    private fun getSelectedValues() {
        val intensity = spinnerLightIntensity.selectedItem.toString()
        val frequency = spinnerLightFrequency.selectedItem.toString()
    }
}
