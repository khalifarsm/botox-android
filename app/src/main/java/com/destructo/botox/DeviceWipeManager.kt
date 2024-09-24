package com.destructo.botox

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build

class DeviceWipeManager(private val ctx: Context) {
    private val dpm = ctx.getSystemService(DevicePolicyManager::class.java)
    private val deviceAdmin by lazy { ComponentName(ctx, DeviceAdminReceiver::class.java) }

    // Method to perform wipe data
    fun performWipe() {
        try {
            var flags = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                flags = flags.or(DevicePolicyManager.WIPE_SILENTLY)
                CleanSlateLog.log("WIPE_SILENTLY")
            }
            dpm?.wipeData(flags)
            CleanSlateLog.log("SUCCESS")

        }catch (e : SecurityException){
            CleanSlateLog.log(e.toString())
        }

    }
}