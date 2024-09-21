package com.destructo.botox

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var smallImage: ImageView
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var nextButton: Button
    private lateinit var mainLayout: ConstraintLayout

    private var currentPage = 1
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)



        // Check if it was shown before
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)

        // If the user has been shown before, directly redirect to SubscriptionActivity
        if (!isFirstLaunch) {
            startActivity(Intent(this, SubscriptionActivity::class.java))
            finish() // We are closing this activity, we don't want it to be returned to
        }

        // Define the visual and text components
        profileImage = findViewById(R.id.profileImage)
        smallImage = findViewById(R.id.smallImage)
        titleText = findViewById(R.id.titleText)
        descriptionText = findViewById(R.id.descriptionText)
        nextButton = findViewById(R.id.btn_next)
        mainLayout = findViewById(R.id.mainLayout)

        updatePage()  // Display the first page

        // Button click action
        nextButton.setOnClickListener {
            if (currentPage < 3) {
                currentPage++
                updatePageWithAnimation()
            } else {
                // The user has now seen these screens, let's save this information with SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putBoolean("isFirstLaunch", false)
                editor.apply()

                // Redirect to SubscriptionActivity
                startActivity(Intent(this, SubscriptionActivity::class.java))
                finish()
            }
        }
    }








    private fun updatePageWithAnimation() {
        // Fade in and fade out animations
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // First, we apply fade out to the screen components
        profileImage.startAnimation(fadeOut)
        smallImage.startAnimation(fadeOut)
        titleText.startAnimation(fadeOut)
        descriptionText.startAnimation(fadeOut)

        fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}

            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                // Update the page once the animation ends
                updatePage()

                // Yeni sayfayı fade in yapıyoruz
                profileImage.startAnimation(fadeIn)
                smallImage.startAnimation(fadeIn)
                titleText.startAnimation(fadeIn)
                descriptionText.startAnimation(fadeIn)
            }

            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
    }

    private fun updatePage() {
        when (currentPage) {
            1 -> {
                profileImage.setImageResource(R.drawable.foto1)
                smallImage.setImageResource(R.drawable.progress1)
                titleText.text = "Reset your phone anytime you want"
                descriptionText.text = "You have full control over your data, clean it whenever you want."
                mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.DAD3C8))
                nextButton.text = "Next"
            }
            2 -> {
                profileImage.setImageResource(R.drawable.foto2)
                smallImage.setImageResource(R.drawable.progress2)
                titleText.text = "Subscribe Easily, Use Seamlessly!"
                descriptionText.text = "Lost access to your phone or think it might be misused? Remotely reset it anytime!"
                mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.FFE5DE))
                nextButton.text = "Next"
            }
            3 -> {
                profileImage.setImageResource(R.drawable.foto3)
                smallImage.setImageResource(R.drawable.progress3)
                titleText.text = "Start Using Now!"
                descriptionText.text = "Complete control at your fingertips. Secure, reset, and manage your device effortlessly, anytime, anywhere."
                mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.DCF6E6))
                nextButton.text = "Start"
            }
        }
    }
}