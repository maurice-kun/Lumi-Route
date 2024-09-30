package hs.fl.lumiroute.fragments;

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R


class ColorSettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_colorsettings, container, false)

        val buttonBack = view.findViewById<Button>(R.id.btnBack)
        buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_colorSettingsFragment_to_settingsFragment)
        }

        return view
    }
}
