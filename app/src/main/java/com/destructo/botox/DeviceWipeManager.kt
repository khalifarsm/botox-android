package com.destructo.botox

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast

class DeviceWipeManager(private val context: Context) {

    private val devicePolicyManager: DevicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val compName: ComponentName = ComponentName(context, DeviceAdminReceiver::class.java)

    // Method to perform wipe data
    fun performWipe() {
        if (devicePolicyManager.isAdminActive(compName)) {
            // Check if the device is locked with a password
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            if (keyguardManager.isKeyguardSecure) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        // SDK level UPSIDE_DOWN_CAKE and above, use wipeDevice
                        devicePolicyManager.wipeDevice(DevicePolicyManager.WIPE_RESET_PROTECTION_DATA)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // SDK level TIRAMISU or below, use wipeData
                        devicePolicyManager.wipeData(DevicePolicyManager.WIPE_RESET_PROTECTION_DATA, "Device wipe initiated by admin")
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        devicePolicyManager.wipeData(DevicePolicyManager.WIPE_RESET_PROTECTION_DATA)
                    } else {
                        devicePolicyManager.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE)
                    }
                } catch (e: SecurityException) {
                    // Handle the case where the app does not have the required permissions
                    Toast.makeText(context, "SecurityException: Permission denied" + e.toString(), Toast.LENGTH_SHORT).show()
                    BotoxLog.log("SecurityException: Permission denied" + e.toString())
                } catch (e: IllegalStateException) {
                    // Handle IllegalStateException if wipe is called on the last full user or system user
                    Toast.makeText(context, "IllegalStateException: Cannot wipe the last full-user", Toast.LENGTH_SHORT).show()
                } catch (e: IllegalArgumentException) {
                    // Handle IllegalArgumentException for invalid input
                    Toast.makeText(context, "IllegalArgumentException: Invalid wipe reason", Toast.LENGTH_SHORT).show()
                }
            } else {
                // If no password is set, wipe data normally
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        devicePolicyManager.wipeDevice(DevicePolicyManager.WIPE_RESET_PROTECTION_DATA)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        devicePolicyManager.wipeData(DevicePolicyManager.WIPE_RESET_PROTECTION_DATA, "Wiping device without password")
                    } else {
                        devicePolicyManager.wipeData(0)
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(context, "SecurityException: Permission denied" + e.toString(), Toast.LENGTH_SHORT).show()
                    BotoxLog.log("SecurityException: Permission denied" + e.toString())
                } catch (e: IllegalStateException) {
                    Toast.makeText(context, "IllegalStateException: Cannot wipe the last full-user", Toast.LENGTH_SHORT).show()
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(context, "IllegalArgumentException: Invalid wipe reason", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Notify the user that they need to enable device admin
            Toast.makeText(context, "Enable Device Admin to perform a wipe", Toast.LENGTH_SHORT).show()
        }
    }
}