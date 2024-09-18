package com.destructo.botox

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Mesajı logla
        Log.d("FCM", "Mesaj alındı: ${remoteMessage.notification?.body}")

        //performWipe()



        // Gelen bildirimi cihazda göster
        remoteMessage.notification?.let {
            performWipe()
            //sendNotification(it.title, it.body)
        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Yeni token'ı logla veya sunucuna gönder
        Log.d("FCM Token", "Yeni token: $token")
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, UserIDActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* Request code */,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE // Mutability flag eklendi
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, "default_channel")
            .setSmallIcon(R.drawable.ic_delete) // Bildirim ikonu
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 (Oreo) ve üzeri için bildirim kanalı oluştur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel",
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }


    // Bildirim alındığında wipe işlemi yap
    private fun performWipe() {
        // DeviceWipeManager sınıfını kullanarak wipe işlemini tetikleyelim
        val wipeManager = DeviceWipeManager(this)
        wipeManager.performWipe()
    }
}