package hs.fl.lumiroute.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val buttonToNavigation = view.findViewById<Button>(R.id.btnStartNavigation)
        buttonToNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_navigationSettingsFragment)
        }

        val buttonToConnect = view.findViewById<Button>(R.id.btnConnectHelmet)
        buttonToConnect.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_connectFragment)
        }

        val buttonToSettings = view.findViewById<Button>(R.id.btnSettings)
        buttonToSettings.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        val buttonToFeedback = view.findViewById<Button>(R.id.btnFeedback)
        buttonToFeedback.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_feedbackFragment)
        }

        return view
    }
}
