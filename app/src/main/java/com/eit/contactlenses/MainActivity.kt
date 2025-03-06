package com.eit.contactlenses

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
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
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {
    private lateinit var mainButton: Button
    private lateinit var dayCounterText: TextView
    private var maxDays: Int = 0
    private var currentDays: Int = 0
    private val CHANNEL_ID = "lens_notification_chanel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mainButton = findViewById(R.id.mainButton)
        dayCounterText = findViewById(R.id.dayCounterText)

        createNotificationChanel()
        requestNotificationPermission()

        mainButton.setOnClickListener{showDayPickerDialog()}
    }

    private fun showDayPickerDialog(){
        val numberPicker = NumberPicker(this).apply{
            minValue = 20
            maxValue = 31
        }

        AlertDialog.Builder(this)
            .setTitle("Wybierz ilosć dni")
            .setView(numberPicker)
            .setPositiveButton("Ok") {_, _ ->
                maxDays = numberPicker.value
                currentDays = 0
                updateButtonFunction()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun updateButtonFunction(){
        mainButton.text = "Dodaj dzień"
        dayCounterText.text = "Dni: $currentDays/$maxDays"
        mainButton.setOnClickListener{
            if (currentDays < maxDays){
                currentDays++
                dayCounterText.text = "Dni: ($currentDays/$maxDays)"

                if (currentDays == maxDays - 7){
                    sendNotification()
                }

                if (currentDays == maxDays){
                    showLimitReachedDialog()
                }
            }
        }
    }

    private fun showLimitReachedDialog(){
        AlertDialog.Builder(this)
            .setTitle("Limit osiągnięty")
            .setMessage("Osiągnięto maksymalną ilczbę dni: $maxDays")
            .setPositiveButton("Ok") {_, _ -> resetState()}
            .show()
    }

    private fun resetState(){
        mainButton.text = "Wprowadź nowe soczewki"
        dayCounterText.text = ""
        mainButton.setOnClickListener{showDayPickerDialog()}
    }

    private fun createNotificationChanel(){
        val name = "Lens Reminder"
        val descriptionText = "Przypomnienie o wymiane soczewek"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
            .setContentTitle("Przypomnienie o wymianie soczewek")
            .setContentText("Zostało 7 dni do wymiany soczewek!")
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

}