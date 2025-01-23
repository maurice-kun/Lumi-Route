package hs.fl.lumiroute.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R

class NavigationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_navigation, container, false)

        val buttonEnd = view.findViewById<Button>(R.id.btnEndNavigation)
        buttonEnd.setOnClickListener {
            findNavController().navigate(R.id.action_navigationFragment_to_feedbackFragment)
        }

        return view
    }
}
