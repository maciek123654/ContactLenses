// === MainActivity.kt === (z uproszczoną funkcjonalnością bez kalendarza)
package com.eit.contactlenses

import DataStoreManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.VibrationEffect
import android.os.VibratorManager
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.gridlayout.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var mainButton: ImageButton
    private lateinit var buttonText: TextView
    private lateinit var dayCounterText: TextView
    private lateinit var vibratorManager: VibratorManager
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var calendarGrid: GridLayout
    private lateinit var monthTextView: TextView
    private lateinit var dataStoreManager: DataStoreManager

    private var displayedMonth: Calendar = Calendar.getInstance()

    private var maxDays: Int = 0
    private var currentDays: Int = 0
    private val CHANNEL_ID = "lens_notification_chanel"


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("LensPreferences", MODE_PRIVATE)

        mainButton = findViewById(R.id.mainButton)
        buttonText = findViewById(R.id.buttonText)
        dayCounterText = findViewById(R.id.dayCounterText)

        vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        buttonText.text = LanguageHelper.getString(this, "insert_lenses_textView")

        maxDays = sharedPreferences.getInt("maxDays", 0)
        currentDays = sharedPreferences.getInt("currentDays", 0)

        if (maxDays > 0){
            dayCounterText.text = LanguageHelper.getString(this, "days_counter").format(currentDays, maxDays)
            buttonText.text = LanguageHelper.getString(this, "add_day")
        }

        createNotificationChanel()
        requestNotificationPermission()

        checkIfTodayIsUsed()

        calendarGrid = findViewById(R.id.calendarGrid)
        monthTextView = findViewById(R.id.monthTextView)
        dataStoreManager = DataStoreManager(this)
        displayedMonth = Calendar.getInstance()


        updateCalendar()

        mainButton.setOnClickListener{
            vibratePhone()

            if (maxDays == 0) {
                showDayPickerDialog()
            } else {
                updateButtonFunction()
                markTodayUsed()
                disableMainButton()
            }
        }
    }

    private fun checkIfTodayIsUsed() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        lifecycleScope.launch {
            DataStoreManager(this@MainActivity).usedDays.collectLatest { usedDays ->
                if (usedDays.contains(todayStr)) {
                    disableMainButton()
                } else {
                    enableMainButton()
                }
                updateCalendar()
            }
        }
    }

    private fun markTodayUsed(){
        val today = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)

        lifecycleScope.launch{
            dataStoreManager.saveUsedDate(todayStr)
            updateCalendar()
        }
    }

    private fun disableMainButton() {
        mainButton.isEnabled = false
        mainButton.alpha = 0.5f
    }

    private fun enableMainButton() {
        mainButton.isEnabled = true
        mainButton.alpha = 1.0f
    }

    private fun showDayPickerDialog(){
        val numberPicker = NumberPicker(this).apply{
            minValue = 2
            maxValue = 31
        }

        val titleView = TextView(this).apply{
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

    private fun updateButtonFunction() {
        buttonText.text = LanguageHelper.getString(this, "add_day")
        dayCounterText.text = LanguageHelper.getString(this, "days_counter").format(currentDays, maxDays)

        if (currentDays < maxDays) {
            currentDays++
            dayCounterText.text = LanguageHelper.getString(this, "days_counter").format(currentDays, maxDays)
            saveToSharedPreferences()

            if (currentDays == maxDays - 7) {
                sendNotification()
            }

            if (currentDays == maxDays) {
                showLimitReachedDialog()
            }
        }
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
            addView(titleView)
            addView(messageView)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Ok") { _, _ ->
                resetState()
            }
            .show()

        val customColor = Color.parseColor("#26AAD3")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(customColor)
    }

    private fun resetState() {
        maxDays = 0
        currentDays = 0
        saveToSharedPreferences()
        buttonText.text = LanguageHelper.getString(this, "insert_lenses_textView")
        dayCounterText.text = ""
    }

    private fun createNotificationChanel(){
        val name = LanguageHelper.getString(this, "reminder_title")
        val descriptionText = LanguageHelper.getString(this, "reminder_title")
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun requestNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    private fun sendNotification() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(LanguageHelper.getString(this, "reminder_title"))
            .setContentText(LanguageHelper.getString(this, "reminder_message"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    private fun vibratePhone(){
        val vibrator = vibratorManager.defaultVibrator
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun saveToSharedPreferences(){
        val editor = sharedPreferences.edit()
        editor.putInt("maxDays", maxDays)
        editor.putInt("currentDays", currentDays)
        editor.apply()
    }

    private fun updateCalendar() {
        //val calendar = Calendar.getInstance()
        val calendar = displayedMonth.clone() as Calendar
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthTextView.text = dateFormat.format(calendar.time).replaceFirstChar { it.uppercase() }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7  // Monday = 0

        calendarGrid.removeAllViews()
        calendarGrid.columnCount = 7

        lifecycleScope.launch {
            val usedDays = dataStoreManager.usedDays.first()
            val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)

            val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
            for (i in 0 until totalCells) {
                val dayLayout = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    }
                    setPadding(8, 8, 8, 8)
                }

                val dayNumber = i - firstDayOfWeek + 1

                if (i < firstDayOfWeek || dayNumber > daysInMonth) {
                    // Pusta komórka
                    dayLayout.addView(TextView(this@MainActivity).apply {
                        text = ""
                        textSize = 16f
                    })
                } else {
                    val dateStr = String.format("%s-%02d", currentMonth, dayNumber)
                    val used = usedDays.contains(dateStr)

                    val dayText = TextView(this@MainActivity).apply {
                        text = dayNumber.toString()
                        textSize = 16f
                        setTextColor(Color.WHITE)
                        typeface = ResourcesCompat.getFont(context, R.font.space_grotesk)
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    }

                    val dot = TextView(this@MainActivity).apply {
                        text = if (used) "•" else ""
                        textSize = 18f
                        setTextColor(Color.WHITE)
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    }

                    dayLayout.addView(dayText)
                    dayLayout.addView(dot)
                }

                calendarGrid.addView(dayLayout)
            }
        }
    }
}