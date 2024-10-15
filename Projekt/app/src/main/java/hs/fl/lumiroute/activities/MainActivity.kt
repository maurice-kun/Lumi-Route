package hs.fl.lumiroute.activities

import android.os.Bundle
import android.content.Context
import android.content.res.Configuration
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import hs.fl.lumiroute.R
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sprache laden und anwenden, bevor das Layout gesetzt wird.
        loadLocale()

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // WindowInsetsListener für Systembars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Methode zum Laden der gespeicherten Sprache
    private fun loadLocale() {
        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = sharedPref.getString("My_Lang", "")
        if (language != null && language.isNotEmpty()) {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val config = Configuration()
            config.setLocale(locale)

            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
}
