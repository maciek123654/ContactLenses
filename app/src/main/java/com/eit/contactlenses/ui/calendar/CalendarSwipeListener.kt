package com.eit.contactlenses.ui.calendar

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.math.abs

class CalendarSwipeListener(private val context: Context, private val calendarManager: CalendarManager) : View.OnTouchListener {

    private val gestureDetector = GestureDetector(context, GestureListener())

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val handled = event?.let { gestureDetector.onTouchEvent(it) } == true

        if (handled) {
            v?.performClick()
        }

        return handled
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,  // <- NULLABLE!
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffX = e2.x - (e1?.x ?: return false)
            val diffY = e2.y - (e1.y)

            if (abs(diffX) > abs(diffY) &&
                abs(diffX) > SWIPE_THRESHOLD &&
                abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
            ) {
                if (diffX > 0) {
                    calendarManager.displayedMonth.add(Calendar.MONTH, -1)
                } else {
                    calendarManager.displayedMonth.add(Calendar.MONTH, 1)
                }

                calendarManager.updateCalendar()
                return true
            }

            return false
        }
    }
}