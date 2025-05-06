package com.eit.contactlenses.ui.menu

import android.content.Intent
import android.graphics.Color
import com.eit.contactlenses.R
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.eit.contactlenses.ui.main.MainActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener{
            onBackPressedMy(it)
        }

        val expandCollapseButton: Button = findViewById(R.id.expandCollapseButton)
        expandCollapseButton.setOnClickListener{
            expandPrivacyPolicy(it)
        }
    }

    fun onBackPressedMy(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Zakończ aktywność
    }

    fun expandPrivacyPolicy(view: View){
        val privacyPolicyText: TextView = findViewById(R.id.privacyPolicyText)
        val button: Button = findViewById(R.id.expandCollapseButton)

        if (privacyPolicyText.maxLines == 9){
            privacyPolicyText.maxLines = Int.MAX_VALUE
            button.text = getString(R.string.privacy_less_en)
        } else {
            privacyPolicyText.maxLines = 9
            button.text = getString(R.string.privacy_more_en)
        }
    }

}