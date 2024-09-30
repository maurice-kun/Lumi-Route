package hs.fl.lumiroute.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R

class FeedbackFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feedback, container, false)

        // Zugriff auf die RatingBar und Buttons
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val buttonSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val buttonBack = view.findViewById<Button>(R.id.btnBack)

        // "Submit"-Button Aktion
        buttonSubmit.setOnClickListener {
            val rating = ratingBar.rating
            Toast.makeText(context, "Rating submitted: $rating stars", Toast.LENGTH_SHORT).show()
            // Hier kannst du die Bewertung speichern oder weiterverarbeiten
        }

        // "Back"-Button Aktion
        buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_feedbackFragment_to_homeFragment)
        }

        return view
    }
}
