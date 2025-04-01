package com.eit.contactlenses.ui.main

import DataStoreManager
import android.Manifest
import android.app.AlertDialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.lifecycleScope
import com.eit.contactlenses.R
import com.eit.contactlenses.ui.calendar.CalendarManager
import com.eit.contactlenses.ui.language.LanguageHelper
import com.eit.contactlenses.util.NotificationUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // === Pola klasy ===
    private lateinit var mainButton: ImageButton
    private lateinit var buttonText: TextView
    private lateinit var dayCounterText: TextView
    private lateinit var vibratorManager: VibratorManager
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var calendarGrid: GridLayout
    private lateinit var monthTextView: TextView
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var calendarManager: CalendarManager

    private var maxDays: Int = 0
    private var currentDays: Int = 0

    // === Lifecycle ===
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inicjalizacja
        sharedPreferences = getSharedPreferences("LensPreferences", MODE_PRIVATE)
        vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        mainButton = findViewById(R.id.mainButton)
        buttonText = findViewById(R.id.buttonText)
        dayCounterText = findViewById(R.id.dayCounterText)
        calendarGrid = findViewById(R.id.calendarGrid)
        monthTextView = findViewById(R.id.monthTextView)

        dataStoreManager = DataStoreManager(this)
        calendarManager = CalendarManager(this, calendarGrid, monthTextView, dataStoreManager, lifecycleScope)

        // Ustawienie wartości z pamięci
        maxDays = sharedPreferences.getInt("maxDays", 0)
        currentDays = sharedPreferences.getInt("currentDays", 0)

        if (maxDays > 0) {
            dayCounterText.text = LanguageHelper.getString(this, "days_counter").format(currentDays, maxDays)
            buttonText.text = LanguageHelper.getString(this, "add_day")
        } else {
            buttonText.text = LanguageHelper.getString(this, "insert_lenses_textView")
        }

        // Notyfikacje
        NotificationUtils.createNotificationChannel(this)
        requestNotificationPermission()

        // Obsługa przycisku
        mainButton.setOnClickListener {
            vibratePhone()
            if (maxDays == 0) {
                showDayPickerDialog()
            } else {
                updateButtonFunction()
                calendarManager.markTodayUsed()
                disableMainButton()
            }
        }

        checkIfTodayIsUsed()
    }

    // === Obsługa kalendarza ===
    private fun checkIfTodayIsUsed() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        lifecycleScope.launch {
            dataStoreManager.usedDays.collectLatest { usedDays ->
                if (usedDays.contains(todayStr)) disableMainButton() else enableMainButton()
                calendarManager.updateCalendar()
            }
        }
    }

    private fun showDayPickerDialog() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 2
            maxValue = 31
        }

        val titleView = TextView(this).apply {
            text = LanguageHelper.getString(this@MainActivity, "choose_days")
            setPadding(40, 20, 40, 20)
            textSize = 20f
            typeface = ResourcesCompat.getFont(this@MainActivity, R.font.space_grotesk)
        }

        val dialog = AlertDialog.Builder(this)
            .setCustomTitle(titleView)
            .setView(numberPicker)
            .setPositiveButton("Ok") { _, _ ->
                maxDays = numberPicker.value
                currentDays = 0
                saveToSharedPreferences()
                buttonText.text = LanguageHelper.getString(this, "add_day")
                dayCounterText.text = LanguageHelper.getString(this, "days_counter").format(currentDays, maxDays)
            }
            .setNegativeButton(LanguageHelper.getString(this, "cancel"), null)
            .create()

        dialog.show()
        val customColor = Color.parseColor("#26AAD3")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(customColor)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(customColor)
    }

    // === Obsługa głównego przycisku ===
    private fun updateButtonFunction() {
        buttonText.text = LanguageHelper.getString(this, "add_day")
        if (currentDays < maxDays) {
            currentDays++
            dayCounterText.text = LanguageHelper.getString(this, "days_counter").format(currentDays, maxDays)
            saveToSharedPreferences()

            if (currentDays == maxDays - 7) {
                NotificationUtils.sendNotification(this)
            }

            if (currentDays == maxDays) {
                showLimitReachedDialog()
            }
        }
    }

    // === Stan licznika ===
    private fun resetState() {
        maxDays = 0
        currentDays = 0
        saveToSharedPreferences()
        buttonText.text = LanguageHelper.getString(this, "insert_lenses_textView")
        dayCounterText.text = ""
    }

    private fun saveToSharedPreferences() {
        sharedPreferences.edit().apply {
            putInt("maxDays", maxDays)
            putInt("currentDays", currentDays)
            apply()
        }
    }

    // === Powiadomienia ===
    private fun requestNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    // === Utils ===
    private fun vibratePhone() {
        vibratorManager.defaultVibrator.vibrate(
            VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    private fun disableMainButton() {
        mainButton.isEnabled = false
        mainButton.alpha = 0.5f
    }

    private fun enableMainButton() {
        mainButton.isEnabled = true
        mainButton.alpha = 1.0f
    }

    private fun showLimitReachedDialog() {
        val titleView = TextView(this).apply {
            text = LanguageHelper.getString(this@MainActivity, "limit_reached")
            setPadding(40, 20, 40, 20)
            textSize = 20f
            typeface = ResourcesCompat.getFont(this@MainActivity, R.font.space_grotesk)
        }

        val messageView = TextView(this).apply {
            text = LanguageHelper.getString(this@MainActivity, "limit_reached_message").format(maxDays)
            setPadding(40, 20, 40, 20)
            textSize = 16f
            typeface = ResourcesCompat.getFont(this@MainActivity, R.font.space_grotesk)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
            calendarGrid.removeAllViews()
            addView(titleView)
            addView(messageView)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Ok") { _, _ -> resetState() }
            .show()

        val customColor = Color.parseColor("#26AAD3")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(customColor)
    }
}