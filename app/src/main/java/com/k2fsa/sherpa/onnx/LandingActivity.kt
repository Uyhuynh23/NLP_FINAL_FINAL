package com.k2fsa.sherpa.onnx

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.airbnb.lottie.LottieAnimationView

class LandingActivity : AppCompatActivity() {

    private lateinit var lottieAnimation: LottieAnimationView
    private lateinit var titleText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var contentContainer: LinearLayout
    private lateinit var btnTryNow: Button
    private lateinit var versionText: TextView
    private lateinit var circle1: View
    private lateinit var circle2: View
    private lateinit var circle3: View
    private lateinit var circle4: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_landing)

        // Make status bar transparent
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        initViews()
        setupInitialState()
        startEntranceAnimations()
        setupClickListeners()
        startFloatingAnimations()
    }

    private fun initViews() {
        lottieAnimation = findViewById(R.id.lottie_animation)
        titleText = findViewById(R.id.title)
        subtitleText = findViewById(R.id.subtitle)
        contentContainer = findViewById(R.id.content_container)
        btnTryNow = findViewById(R.id.btn_try_now)
        versionText = findViewById(R.id.version_text)
        circle1 = findViewById(R.id.circle1)
        circle2 = findViewById(R.id.circle2)
        circle3 = findViewById(R.id.circle3)
        circle4 = findViewById(R.id.circle4)
    }

    private fun setupInitialState() {
        // Set initial alpha to 0 for fade-in effect
        lottieAnimation.alpha = 0f
        lottieAnimation.scaleX = 0.5f
        lottieAnimation.scaleY = 0.5f

        titleText.alpha = 0f
        titleText.translationY = 50f

        subtitleText.alpha = 0f
        subtitleText.translationY = 30f

        contentContainer.alpha = 0f
        contentContainer.translationY = 60f

        btnTryNow.alpha = 0f
        btnTryNow.scaleX = 0.8f
        btnTryNow.scaleY = 0.8f

        versionText.alpha = 0f
    }

    private fun startEntranceAnimations() {
        val handler = Handler(Looper.getMainLooper())

        // Lottie animation entrance
        handler.postDelayed({
            animateLottieEntrance()
        }, 200)

        // Title animation
        handler.postDelayed({
            animateViewEntrance(titleText, 400)
        }, 500)

        // Subtitle animation
        handler.postDelayed({
            animateViewEntrance(subtitleText, 350)
        }, 700)

        // Content container (features list)
        handler.postDelayed({
            animateViewEntrance(contentContainer, 500)
        }, 900)

        // CTA Button animation
        handler.postDelayed({
            animateButtonEntrance()
        }, 1200)

        // Version text
        handler.postDelayed({
            versionText.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }, 1400)
    }

    private fun animateLottieEntrance() {
        val scaleX = ObjectAnimator.ofFloat(lottieAnimation, "scaleX", 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(lottieAnimation, "scaleY", 0.5f, 1f)
        val alpha = ObjectAnimator.ofFloat(lottieAnimation, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 600
            interpolator = OvershootInterpolator(1.2f)
            start()
        }
    }

    private fun animateViewEntrance(view: View, duration: Long) {
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun animateButtonEntrance() {
        val scaleX = ObjectAnimator.ofFloat(btnTryNow, "scaleX", 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(btnTryNow, "scaleY", 0.8f, 1f)
        val alpha = ObjectAnimator.ofFloat(btnTryNow, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 400
            interpolator = OvershootInterpolator(2f)
            start()
        }
    }

    private fun startFloatingAnimations() {
        // Animate decorative circles with floating effect
        animateFloating(circle1, 15f, 4000)
        animateFloating(circle2, -12f, 3500)
        animateFloating(circle3, 10f, 3000)
        animateFloating(circle4, -8f, 2500)
    }

    private fun animateFloating(view: View, distance: Float, duration: Long) {
        val animator = ObjectAnimator.ofFloat(view, "translationY", 0f, distance, 0f)
        animator.duration = duration
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()

        // Also add slight horizontal movement
        val horizontalAnimator = ObjectAnimator.ofFloat(view, "translationX", 0f, distance / 2, 0f)
        horizontalAnimator.duration = (duration * 1.2).toLong()
        horizontalAnimator.repeatCount = ObjectAnimator.INFINITE
        horizontalAnimator.interpolator = AccelerateDecelerateInterpolator()
        horizontalAnimator.start()
    }

    private fun setupClickListeners() {
        btnTryNow.setOnClickListener {
            // Button press animation
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction {
                            navigateToMain()
                        }
                        .start()
                }
                .start()
        }
    }

    private fun navigateToMain() {
        // Exit animation before navigation
        val exitDuration = 300L

        lottieAnimation.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(exitDuration)
            .start()

        contentContainer.animate()
            .alpha(0f)
            .translationY(-30f)
            .setDuration(exitDuration)
            .start()

        btnTryNow.animate()
            .alpha(0f)
            .translationY(30f)
            .setDuration(exitDuration)
            .withEndAction {
                // Navigate to LoadingActivity instead of MainActivity
                val intent = Intent(this, LoadingActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            .start()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
