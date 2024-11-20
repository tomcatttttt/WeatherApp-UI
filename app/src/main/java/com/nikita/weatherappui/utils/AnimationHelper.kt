package com.nikita.weatherappui.utils

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

object AnimationHelper {
    fun animateVisibility(view: View, visibility: Int) {
        val alpha = if (visibility == View.VISIBLE) 1f else 0f
        val animator = ObjectAnimator.ofFloat(view, "alpha", alpha).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }

        animator.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {
                if (visibility == View.VISIBLE) {
                    view.visibility = View.VISIBLE
                }
            }

            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (visibility == View.GONE) {
                    view.visibility = View.GONE
                }
            }

            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })

        animator.start()
    }
}