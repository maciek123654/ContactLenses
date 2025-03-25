package com.eit.contactlenses

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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.VibrationEffect
import android.os.VibratorManager
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import org.w3c.dom.Text
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var mainButton: ImageButton
    private lateinit var buttonText: TextView
    private lateinit var dayCounterText: TextView
    private lateinit var vibratorManager: VibratorManager
    private lateinit var sharedPreferences: SharedPreferences
    private var maxDays: Int = 0
    private var currentDays: Int = 0
    private val CHANNEL_ID = "lens_notification_chanel"

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LanguageHelper.applySavedLanguage(it) } ?: newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("LensPreferences", MODE_PRIVATE)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val calendarView = findViewById<CalendarView>(R.id.CalendarView)
        calendarView.loadUsedDays(this)

        mainButton = findViewById(R.id.mainButton)
        buttonText = findViewById(R.id.buttonText)
        dayCounterText = findViewById(R.id.dayCounterText)

        vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        buttonText.text = LanguageHelper.getString(this, "insert_lenses_textView")

        maxDays = sharedPreferences.getInt("maxDays", 0)
        currentDays = sharedPreferences.getInt("currentDays", 0)

        val monthTextView = findViewById<TextView>(R.id.monthTextView)
        calendarView.setMonthTextView(monthTextView)

        if (maxDays > 0){
            dayCounterText.text = LanguageHelper.getString(this, "days_counter").format(currentDays, maxDays)
            buttonText.text = LanguageHelper.getString(this, "add_day")
        }

        createNotificationChanel()
        requestNotificationPermission()

        mainButton.setOnClickListener{
            vibratePhone()

            if (maxDays == 0) {
                showDayPickerDialog()
            } else if (maxDays != 0) {
                updateButtonFunction()
                calendarView.markCurrentDayUsed()
                //vibratePhone()
            }
        }
    }

    private fun showDayPickerDialog(){
        val numberPicker = NumberPicker(this).apply{
            minValue = 20
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
                mainButton.setOnClickListener {
                    updateButtonFunction()
                }

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

        maxDays = sharedPreferences.getInt("maxDays", 0)
        currentDays = sharedPreferences.getInt("currentDays", 0)
        buttonText.text = LanguageHelper.getString(this, "insert_lenses_textView")
        dayCounterText.text = ""

        mainButton.setOnClickListener {
            showDayPickerDialog()
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
}