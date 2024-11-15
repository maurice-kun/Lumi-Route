package hs.fl.lumiroute.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val buttonLanguage = view.findViewById<Button>(R.id.btnLanguage)
        buttonLanguage.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_languageSettingsFragment)
        }

        val buttonColor = view.findViewById<Button>(R.id.btnColorSettings)
        buttonColor.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_colorSettingsFragment)
        }

        val buttonLight = view.findViewById<Button>(R.id.btnLightSettings)
        buttonLight.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_lightSettingsFragment)
        }

        val buttonDebug = view.findViewById<Button>(R.id.btnDebug)
        buttonDebug.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_debugFragment)
        }

        val buttonTest = view.findViewById<Button>(R.id.btnTest)
        buttonTest.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_testFragment)
        }

        val buttonBack = view.findViewById<Button>(R.id.btnBack)
        buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_homeFragment)
        }

        return view
    }
}
