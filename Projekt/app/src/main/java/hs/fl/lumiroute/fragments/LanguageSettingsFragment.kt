package hs.fl.lumiroute.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import hs.fl.lumiroute.R
import java.util.*

class LanguageSettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lngsettings, container, false)

        val buttonEnglish = view.findViewById<Button>(R.id.btnEnglish)
        val buttonGerman = view.findViewById<Button>(R.id.btnGerman)
        val buttonFrench = view.findViewById<Button>(R.id.btnFrench)
        val buttonJapanese = view.findViewById<Button>(R.id.btnJapanese)
        val buttonBack = view.findViewById<Button>(R.id.btnBack)

        buttonEnglish.setOnClickListener {
            setLocale("en")
        }

        buttonGerman.setOnClickListener {
            setLocale("de")
        }

        buttonFrench.setOnClickListener {
            setLocale("fr")
        }

        buttonJapanese.setOnClickListener {
            setLocale("jp")
        }

        buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_languageSettingsFragment_to_settingsFragment)
        }

        return view
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        // Speichern der Sprache in SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("My_Lang", languageCode)
        editor.apply()

        requireActivity().resources.updateConfiguration(
            config,
            requireActivity().resources.displayMetrics
        )
        requireActivity().recreate()
    }
}
