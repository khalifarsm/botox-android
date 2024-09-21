package com.destructo.botox

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class UserIDActivity : AppCompatActivity() {

    private lateinit var tvSubscriptionCode: TextView
    private lateinit var tvResetCodeHash: TextView
    private lateinit var tvPassword: TextView
    private lateinit var tvFcmToken: TextView
    private lateinit var tvUserId: TextView

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var compName: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_idactivity)

        tvUserId = findViewById(R.id.tv_user_id)

        // Shared Preferences'ten verileri çek
        val sharedPreferences = getSharedPreferences("com.destructo.botox.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        val subscriptionCode = sharedPreferences.getString("subscriptionCode", "N/A")
        val resetCodeHash = sharedPreferences.getString("resetCodeHash", "N/A")
        val password = sharedPreferences.getString("password", "N/A")
        val fcmToken = sharedPreferences.getString("fcmToken", "N/A")
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