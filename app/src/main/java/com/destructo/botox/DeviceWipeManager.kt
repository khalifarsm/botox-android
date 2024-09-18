package com.destructo.botox

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class DeviceWipeManager(private val context: Context) {

    private val devicePolicyManager: DevicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val compName: ComponentName = ComponentName(context, DeviceAdminReceiver::class.java)

    // Method to perform wipe data
    fun performWipe() {
        if (devicePolicyManager.isAdminActive(compName)) {
            // Check if the device is locked with a password
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            if (keyguardManager.isKeyguardSecure) {
                // Device is locked with a password, wipe the data
                devicePolicyManager.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE) // You can also add flags like WIPE_RESET_PROTECTION_DATA
            } else {
                // If no password, just wipe normally
                devicePolicyManager.wipeData(0)
            }
        } else {
            // Notify the user that they need to enable device admin
            // (optional: you can remove this if you want to silently fail)
            Toast.makeText(context, "Enable Device Admin to perform a wipe", Toast.LENGTH_SHORT).show()
        }
    }
}