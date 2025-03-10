package com.eit.contactlenses

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LanguageSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLanguageSelectionDialog()
    }

    private fun showLanguageSelectionDialog(){
        val languages = arrayOf("Polski", "English")
        val languageCodes = arrayOf("pl", "en")

        AlertDialog.Builder(this)
            .setTitle(LanguageHelper.getString(this, "select_language"))
            .setItems(languages){_, which ->
                LanguageHelper.setLanguage(this, languageCodes[which])
                LanguageHelper.setFirstLaunchCompleted(this)

                restartApp()
            }
            .setCancelable(false)
            .show()
    }

    private fun restartApp(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }
}
