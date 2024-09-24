package com.destructo.botox

import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class UserIDActivity : AppCompatActivity() {

    //private lateinit var tvSubscriptionCode: TextView
    //private lateinit var tvResetCodeHash: TextView
    //private lateinit var tvPassword: TextView
    //private lateinit var tvFcmToken: TextView
    private lateinit var tvUserId: TextView
    private lateinit var btnResetPhone: Button
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var compName: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_idactivity)

        tvUserId = findViewById(R.id.tv_user_id)

        sharedPreferences = getSharedPreferences("com.destructo.botox.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        //val subscriptionCode = sharedPreferences.getString("subscriptionCode", "N/A")
        //val resetCodeHash = sharedPreferences.getString("resetCodeHash", "N/A")
        //val password = sharedPreferences.getString("password", "N/A")
        //val fcmToken = sharedPreferences.getString("fcmToken", "N/A")
        val userId = sharedPreferences.getString("userId", "N/A")


        val serviceIntent = Intent(this, MyForegroundService::class.java)
        startService(serviceIntent)

        //tvSubscriptionCode.text = "Subscription Code: $subscriptionCode"
        //tvResetCodeHash.text = "Reset Code Hash: $resetCodeHash"
        //tvPassword.text = "Password: $password"
        //tvFcmToken.text = "FCM Token: $fcmToken"
        tvUserId.text = "User ID: $userId"

        // Device Policy Manager and ComponentName initialization
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, DeviceAdminReceiver::class.java)


        checkAdminPermission()

        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This app needs device admin permissions to wipe data.")
        startActivity(intent)


        showWelcomeMessage()
        btnResetPhone = findViewById(R.id.btn_reset_phone)
        btnResetPhone.setOnClickListener {
            showHowToUseDialog()
        }

    }

    private fun showWelcomeMessage() {
        val isFirstTime = sharedPreferences.getBoolean("first_time_user_id_screen", true)
        if (isFirstTime) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Congratulations!")
            builder.setMessage(
                "Congratulations! Your registration has been successfully completed. You can now use your phone securely and reset it remotely whenever needed. You will need your User ID for the reset process. Don't forget to store your User ID in a safe place."
            )
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()

            // Artık bu mesajı bir daha göstermemek için SharedPreferences'a kaydediyoruz
            sharedPreferences.edit().putBoolean("first_time_user_id_screen", false).apply()
        }
    }

    private fun showHowToUseDialog() {
        val builder = AlertDialog.Builder(this)

        // Link yerine göstermek istediğiniz metin
        val message = SpannableString("When you want to remotely reset your phone, you can reset it using this link. For the reset process, you will use the password you set during registration and your User ID. Be sure to store your User ID in a safe place!")

        // "this link" kısmını tıklanabilir ve altı çizili hale getirme
        val clickableText = "this link"
        val startIndex = message.indexOf(clickableText)
        val endIndex = startIndex + clickableText.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Link tıklanınca tarayıcıda açılır
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(Configuration.RESET_URL))
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@UserIDActivity, android.R.color.holo_blue_light) // Link rengi
                ds.isUnderlineText = true // Altı çizili yap
            }
        }

        // "this link" kısmını SpannableString'e ekliyoruz
        message.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // AlertDialog içinde metni gösterecek TextView
        val textView = TextView(this).apply {
            text = message
            movementMethod = LinkMovementMethod.getInstance() // Link tıklanabilir hale gelir
            setPadding(40, 20, 40, 20)
        }

        // AlertDialog'u oluşturma
        builder.setTitle("How to Use")
        builder.setView(textView)

        // "OK" butonu
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        // "Copy Link" butonu (Linki kopyalamak için)
        builder.setNeutralButton("Copy Link") { dialog, _ ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Reset Link", Configuration.RESET_URL)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        builder.create().show()
    }

    // Method to check and request device admin permission
    private fun checkAdminPermission() {
        if (!devicePolicyManager.isAdminActive(compName)) {
            // If not active as a device admin, request it
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This app requires device admin rights to perform a wipe.")
            }
            startActivityForResult(intent, 1000)
        }
    }


}