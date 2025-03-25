package com.eit.contactlenses

import DataStoreManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ScaleDrawable
import android.health.connect.datatypes.units.Velocity
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarView(context: Context, attrs: AttributeSet?) : GridLayout(context, attrs), GestureDetector.OnGestureListener {
    private val dataStoreManager = DataStoreManager(context)
    private val calendar = Calendar.getInstance()
    private var daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    private var startDayOfWeek = getStartDayOfWeek()
    private val dayViews = mutableListOf<TextView>()
    private val currentDay: Int = calendar.get(Calendar.DAY_OF_MONTH)
    private var usedDays: MutableSet<String> = mutableSetOf()
    private var currentMonth: String = getCurrentMonth()

    private val gestureDetector = GestureDetector(context, this)

    init {
        columnCount = 7
        rowCount = 6
        setupCalendar()

        setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }

    private fun setupCalendar() {
        dayViews.clear()
        removeAllViews()

        for (i in 0 until columnCount * rowCount) {
            val textView = TextView(context).apply {
                textSize = 18f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(10, 10, 10, 10)
                typeface = ResourcesCompat.getFont(context, R.font.space_grotesk)
                layoutParams = LayoutParams().apply {
                    width = 0
                    height = 150
                    columnSpec = spec(UNDEFINED, 1f)
                    rowSpec = spec(UNDEFINED, 1f)
                }
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#26AAD3"))
                    cornerRadius = 20f
                }
            }
            dayViews.add(textView)
            addView(textView)
        }

        for (i in 0 until startDayOfWeek) {
            dayViews[i].text = ""
        }

        for (day in 1..daysInMonth) {
            val index = startDayOfWeek + day - 1
            dayViews[index].text = day.toString()

            if (day == currentDay) {
                val border = GradientDrawable().apply {
                    setColor(Color.parseColor("#26AAD3"))
                    setStroke(5, Color.BLACK)
                    cornerRadius = 20f
                }
                dayViews[index].background = border
            }

            if (usedDays.contains("$currentMonth-$day")) {
                addMarker(dayViews[index], day)
            }
        }
    }

    fun markCurrentDayUsed() {
        val index = startDayOfWeek + currentDay - 1
        addMarker(dayViews[index], currentDay)

        usedDays.add("$currentMonth-$currentDay")

        (context as? LifecycleOwner)?.lifecycleScope?.launch {
            dataStoreManager.saveUsedDay(currentDay)
        }
    }

    fun loadUsedDays(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            dataStoreManager.usedDays.collectLatest { days ->
                usedDays = days.toMutableSet()
                refreshCalendar()
            }
        }
    }

    fun changeMonth(offset: Int) {
        calendar.add(Calendar.MONTH, offset)
        daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        startDayOfWeek = getStartDayOfWeek()
        currentMonth = getCurrentMonth()
        loadUsedDays(context as LifecycleOwner)
    }

    private fun refreshCalendar() {
        for (i in 0 until columnCount * rowCount) {
            dayViews[i].text = ""
            dayViews[i].background = GradientDrawable().apply {
                setColor(Color.parseColor("#26AAD3"))
                cornerRadius = 20f
            }
        }

        for (day in 1..daysInMonth) {
            val index = startDayOfWeek + day - 1
            dayViews[index].text = day.toString()

            val isCurrentMonth = calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                   calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)

            if (day == currentDay && isCurrentMonth) {
                val border = GradientDrawable().apply {
                    setColor(Color.parseColor("#26AAD3"))
                    setStroke(5, Color.BLACK)
                    cornerRadius = 20f
                }
                dayViews[index].background = border
            }

            if (usedDays.contains("$currentMonth-$day")) {
                addMarker(dayViews[index], day)
            }
        }
    }


    private fun getCurrentMonth(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
    }

    private fun getStartDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        cal.time = calendar.time
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    private fun addMarker(dayView: TextView, day: Int) {
        val text = SpannableString("$day\n\u25CF")
        text.setSpan(SuperscriptSpan(), day.toString().length, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(RelativeSizeSpan(0.8f), day.toString().length, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        dayView.text = text
    }

    //Gesture detector

    override fun onDown(p0: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent) {
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return true
    }

    override fun onScroll(
        p0: MotionEvent?,
        p1: MotionEvent,
        p2: Float,
        p3: Float
    ): Boolean {
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
    }

    override fun onFling(
        p0: MotionEvent?,
        p1: MotionEvent,
        p2: Float,
        p3: Float
    ): Boolean {
        if(p0 != null && p1 != null){
            val deltaX = p1.x - p0.x

            if (deltaX > 100){
                changeMonth(-1)
                return true
            } else if (deltaX < -100){
                changeMonth(1)
                return true
            }
        }
        return false
    }
}