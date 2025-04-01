package com.eit.contactlenses.ui.calendar

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.gridlayout.widget.GridLayout
import com.eit.contactlenses.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import DataStoreManager

class CalendarManager(
    private val context: Context,
    private val calendarGrid: GridLayout,
    private val monthTextView: TextView,
    private val dataStoreManager: DataStoreManager,
    private val coroutineScope: CoroutineScope
) {
    var displayedMonth: Calendar = Calendar.getInstance()

    fun updateCalendar() {
        val calendar = displayedMonth.clone() as Calendar
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthTextView.text = dateFormat.format(calendar.time).replaceFirstChar { it.uppercase() }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7

        calendarGrid.removeAllViews()
        calendarGrid.columnCount = 7

        coroutineScope.launch {
            val usedDays = dataStoreManager.usedDays.first()
            val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)

            val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
            for (i in 0 until totalCells) {
                val dayLayout = LinearLayout(context).apply {
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
                    dayLayout.addView(TextView(context).apply {
                        text = ""
                        textSize = 16f
                    })
                } else {
                    val dateStr = String.format("%s-%02d", currentMonth, dayNumber)
                    val used = usedDays.contains(dateStr)

                    val dayText = TextView(context).apply {
                        text = dayNumber.toString()
                        textSize = 16f
                        setTextColor(Color.WHITE)
                        typeface = ResourcesCompat.getFont(context, R.font.space_grotesk)
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    }

                    val dot = TextView(context).apply {
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

    fun markTodayUsed() {
        val today = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)

        coroutineScope.launch {
            dataStoreManager.saveUsedDate(todayStr)
        }
    }
}