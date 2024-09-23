package com.destructo.botox

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Base64
import android.widget.Toast
import androidx.core.app.NotificationCompat
import okhttp3.*
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class MyForegroundService : Service() {

    private lateinit var webSocket: WebSocket
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS) // Send a ping every 30 seconds
        .readTimeout(0, TimeUnit.MILLISECONDS)  // No timeout while waiting for messages from the server
        .build()

    private val handler = Handler()

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        // Create a notification channel (for Android O and above)
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
            .setContentTitle("Botox")
            .setContentText("Botox is Running for Your Security")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }

        startWebSocketConnection()
    }

    private fun startWebSocketConnection() {
        // Retrieve userId and password from SharedPreferences
        val sharedPreferences = getSharedPreferences("com.destructo.botox.PREFERENCE_FILE_KEY", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "N/A") ?: ""
        val password = sharedPreferences.getString("password", "N/A") ?: ""
        val localResetHash = sharedPreferences.getString("resetCodeHash", "N/A") ?: ""
        BotoxLog.log("localResetHash: " + localResetHash)


        BotoxLog.log("llk user:" +userId)
        BotoxLog.log("llk password" +password)

        //Base64 encoding
        val credentials = "$userId:$password"
        val basicAuth = Base64.encodeToString(credentials.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)

        // Initiate connection with the WebSocket server
        val url = "${Configuration.WEBSOCKET_URL}$basicAuth"

        //val request = Request.Builder().url("ws://192.168.68.128:81").build()
        val request = Request.Builder().url(url).build()

        BotoxLog.log("llk link: $url")
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                BotoxLog.log("llk Bağlantı açıldı.")
                webSocket.send("ping!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                BotoxLog.log("llk Alınan mesaj: $text")

                // Process the incoming JSON message
                val jsonObject = JSONObject(text)
                val receivedReset = jsonObject.getString("reset")
                val reseiveResetHash = HashUtil.generateHash(receivedReset)
                val receivedUserId = jsonObject.getString("userId")
                val after = jsonObject.getInt("after") + 1

                // Retrieve the local hash (default from resetCodeHash)

                // Compare the reset hashes
                if (receivedUserId == userId && localResetHash == reseiveResetHash) {
                    BotoxLog.log("llk Reset işlemi planlanıyor. $after saniye sonra.")
                    handler.postDelayed({
                        performWipe()
                    }, (after * 1000).toLong())
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                BotoxLog.log("llk Bağlantı kapandı: $reason")
                // Reconnect if the connection is closed
                reconnectWebSocket()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                BotoxLog.log("llk Bağlantı hatası: ${t.message}")
                // Reconnect in case of a connection failure
                reconnectWebSocket()
            }
        })
    }

    private fun reconnectWebSocket() {
        BotoxLog.log("llk Yeniden bağlanmaya çalışılıyor...")
        startWebSocketConnection()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // The service will continuously run here and keep listening to the WebSocket connection
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket.close(1000, "Servis kapatılıyor")
        BotoxLog.log("llk Servis durduruldu.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun performWipe() {
        BotoxLog.log("llk Cihaz sıfırlama işlemi başlatılıyor...")
        //Toast.makeText(applicationContext, "Factory Reset Process", Toast.LENGTH_SHORT).show()
        // Trigger the wipe operation using the DeviceWipeManager class
        val wipeManager = DeviceWipeManager(this)
        wipeManager.performWipe()
    }
}