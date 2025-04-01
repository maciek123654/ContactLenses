package com.eit.contactlenses.ui.language

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.eit.contactlenses.ui.main.MainActivity

const val item1 = android.R.layout.simple_list_item_1

class LanguageSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLanguageSelectionDialog()
    }

    private fun showLanguageSelectionDialog(){
        val languages = arrayOf("Polski", "English")
        val languageCodes = arrayOf("pl", "en")

        val adapter = ArrayAdapter(this, item1, languages).apply {
            setDropDownViewResource(item1)
        }

        val listView = ListView(this).apply {
            this.adapter = adapter
            this.dividerHeight = 1
        }

        val titleView = TextView(this).apply{
            text = LanguageHelper.getString(this@LanguageSelectionActivity, "select_language")
            setPadding(40, 20, 40, 20)
            textSize = 20f
            typeface = ResourcesCompat.getFont(this@LanguageSelectionActivity, com.eit.contactlenses.R.font.space_grotesk)
        }

        val dialog = AlertDialog.Builder(this)
            .setCustomTitle(titleView)
            .setView(listView)
            .setCancelable(false)
            .create()

        listView.setOnItemClickListener { _, _, which, _ ->
            LanguageHelper.setLanguage(this, languageCodes[which])
            LanguageHelper.setFirstLaunchCompleted(this)
            dialog.dismiss()
            restartApp()
        }

        dialog.show()
    }

    private fun restartApp(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }
}