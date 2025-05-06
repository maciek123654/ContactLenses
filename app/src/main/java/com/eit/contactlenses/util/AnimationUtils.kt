package com.eit.contactlenses.util

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd

object AnimationUtils {
/*
Animacja ikony ustawień i przejście do ekranu ustawień 0.3 sekundy po kliknięciu
*/
    fun rotateAndNavigate(view: View, duration: Long = 300, onEnd: () -> Unit){
        val rotate = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).apply{
            this.duration = duration
            interpolator = LinearInterpolator()
            doOnEnd { onEnd() }
        }
    rotate.start()
    }
}