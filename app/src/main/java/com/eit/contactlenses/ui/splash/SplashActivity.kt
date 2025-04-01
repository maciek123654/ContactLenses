package com.eit.contactlenses.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.eit.contactlenses.ui.language.LanguageHelper
import com.eit.contactlenses.ui.language.LanguageSelectionActivity
import com.eit.contactlenses.R
import com.eit.contactlenses.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val nextActivity = if (LanguageHelper.isFirstLaunch(this)) {
            LanguageSelectionActivity::class.java
        } else {
            MainActivity::class.java
        }

        val intent = Intent(this, nextActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}