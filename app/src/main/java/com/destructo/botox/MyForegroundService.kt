package com.destructo.botox

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class MyForegroundService : Service() {

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "foreground_service_channel",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, "foreground_service_channel")
            .setContentTitle("Uygulama Arka Planda Çalışıyor")
            .setContentText("Firebase bildirimleri dinleniyor.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Servis burada sürekli çalışacak ve işlemleri yapacak
        // Firebase'den gelen bildirimler burada işlenebilir veya belirli bir kod sürekli çalışabilir
        performWipe()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun performWipe() {
        Log.d("ForegroundService", "Cihaz sıfırlama işlemi başlatılıyor...")
        // Burada istediğin işlemleri gerçekleştirebilirsin
    }
}