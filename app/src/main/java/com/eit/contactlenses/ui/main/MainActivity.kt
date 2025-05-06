package com.eit.contactlenses.ui.main

import com.eit.contactlenses.util.AnimationUtils
import DataStoreManager
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
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
import com.eit.contactlenses.ui.calendar.CalendarSwipeListener
import com.eit.contactlenses.ui.menu.SettingsActivity
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
    @SuppressLint("ClickableViewAccessibility")
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

        calendarGrid.setOnTouchListener(CalendarSwipeListener(this, calendarManager))

        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener{
            AnimationUtils.rotateAndNavigate(settingsButton) {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }

        // Ustawienie wartości z pamięci
        maxDays = sharedPreferences.getInt("maxDays", 0)
        currentDays = sharedPreferences.getInt("currentDays", 0)

        if (maxDays > 0) {
            dayCounterText.text = getString(R.string.days_counter_en).format(currentDays, maxDays)
            buttonText.text = getString(R.string.add_day_en)
        } else {
            buttonText.text = getString(R.string.insert_lenses_textView_en)
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
            minValue = 20
            maxValue = 31
        }

        val titleView = TextView(this).apply {
            text = getString(R.string.choose_days_en)
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
                buttonText.text = getString(R.string.add_day_en)
                dayCounterText.text = getString(R.string.days_counter_en).format(currentDays, maxDays)
            }
            .setNegativeButton(getString(R.string.cancel_en), null)
            .create()

        dialog.show()
        val customColor = Color.parseColor("#26AAD3")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(customColor)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(customColor)
    }

    // === Obsługa głównego przycisku ===
    private fun updateButtonFunction() {
        buttonText.text = getString(R.string.add_day_en)
        if (currentDays < maxDays) {
            currentDays++
            dayCounterText.text = getString(R.string.days_counter_en).format(currentDays, maxDays)
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
        buttonText.text = getString(R.string.insert_lenses_textView_en)
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
            text = getString(R.string.limit_reached_en)
            setPadding(40, 20, 40, 20)
            textSize = 20f
            typeface = ResourcesCompat.getFont(this@MainActivity, R.font.space_grotesk)
        }

        val messageView = TextView(this).apply {
            text = getString(R.string.limit_reached_message_en)
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