package com.k2fsa.sherpa.onnx

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.airbnb.lottie.LottieAnimationView

class LoadingActivity : AppCompatActivity() {

    private lateinit var lottieLoading: LottieAnimationView
    private lateinit var loadingTitle: TextView
    private lateinit var loadingStatus: TextView
    private lateinit var tipText: TextView
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private lateinit var glow1: View
    private lateinit var glow2: View

    private val handler = Handler(Looper.getMainLooper())
    private val statusMessages = listOf(
        "Loading voice model...",
        "Initializing neural network...",
        "Preparing audio engine...",
        "Almost ready..."
    )
    private var currentStatusIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContentView(R.layout.activity_loading)

        initViews()
        startEntranceAnimations()
        startDotsAnimation()
        startStatusRotation()
        startGlowAnimation()

        // Simulate loading and navigate to MainActivity
        handler.postDelayed({
            navigateToMain()
        }, 2000) // 3 seconds loading time
    }

    private fun initViews() {
        lottieLoading = findViewById(R.id.lottie_loading)
        loadingTitle = findViewById(R.id.loading_title)
        loadingStatus = findViewById(R.id.loading_status)
        tipText = findViewById(R.id.tip_text)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)
        glow1 = findViewById(R.id.glow1)
        glow2 = findViewById(R.id.glow2)
    }

    private fun startEntranceAnimations() {
        // Fade in all elements
        val views = listOf(lottieLoading, loadingTitle, loadingStatus, tipText)
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 30f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay((index * 100).toLong())
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    private fun startDotsAnimation() {
        val dots = listOf(dot1, dot2, dot3)

        dots.forEachIndexed { index, dot ->
            val scaleUp = ObjectAnimator.ofFloat(dot, "scaleX", 1f, 1.5f, 1f)
            val scaleUpY = ObjectAnimator.ofFloat(dot, "scaleY", 1f, 1.5f, 1f)
            val alpha = ObjectAnimator.ofFloat(dot, "alpha", 0.5f, 1f, 0.5f)

            AnimatorSet().apply {
                playTogether(scaleUp, scaleUpY, alpha)
                duration = 600
                startDelay = (index * 200).toLong()
                interpolator = AccelerateDecelerateInterpolator()

                // Repeat animation
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        if (!isFinishing) {
                            startDelay = 0
                            start()
                        }
                    }
                })
                start()
            }
        }
    }

    private fun startStatusRotation() {
        val rotateStatus = object : Runnable {
            override fun run() {
                if (!isFinishing) {
                    // Fade out current status
                    loadingStatus.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction {
                            currentStatusIndex = (currentStatusIndex + 1) % statusMessages.size
                            loadingStatus.text = statusMessages[currentStatusIndex]
                            // Fade in new status
                            loadingStatus.animate()
                                .alpha(1f)
                                .setDuration(200)
                                .start()
                        }
                        .start()

                    handler.postDelayed(this, 1500)
                }
            }
        }
        handler.postDelayed(rotateStatus, 1500)
    }

    private fun startGlowAnimation() {
        // Pulsing glow effect
        val pulseGlow1 = ObjectAnimator.ofFloat(glow1, "alpha", 0.3f, 0.6f, 0.3f)
        pulseGlow1.duration = 2000
        pulseGlow1.repeatCount = ObjectAnimator.INFINITE
        pulseGlow1.interpolator = AccelerateDecelerateInterpolator()
        pulseGlow1.start()

        val pulseGlow2 = ObjectAnimator.ofFloat(glow2, "alpha", 0.2f, 0.5f, 0.2f)
        pulseGlow2.duration = 2500
        pulseGlow2.repeatCount = ObjectAnimator.INFINITE
        pulseGlow2.interpolator = AccelerateDecelerateInterpolator()
        pulseGlow2.start()

        // Slow rotation for glow1
        val rotateGlow = ObjectAnimator.ofFloat(glow1, "rotation", 0f, 360f)
        rotateGlow.duration = 20000
        rotateGlow.repeatCount = ObjectAnimator.INFINITE
        rotateGlow.interpolator = android.view.animation.LinearInterpolator()
        rotateGlow.start()
    }

    private fun navigateToMain() {
        // Update status to complete
        loadingStatus.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                loadingStatus.text = "✓ Ready!"
                loadingStatus.setTextColor(resources.getColor(R.color.success, theme))
                loadingStatus.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()

        // Exit animation
        handler.postDelayed({
            val views = listOf(lottieLoading, loadingTitle, loadingStatus, tipText, dot1, dot2, dot3)
            views.forEachIndexed { index, view ->
                view.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(300)
                    .setStartDelay((index * 30).toLong())
                    .withEndAction {
                        if (index == views.lastIndex) {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            finish()
                        }
                    }
                    .start()
            }
        }, 500)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Disable back button during loading
    }
}

