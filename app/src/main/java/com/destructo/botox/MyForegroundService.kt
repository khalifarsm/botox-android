package com.destructo.botox

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Base64
import androidx.core.app.NotificationCompat
import okhttp3.*
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class MyForegroundService : Service() {

    private lateinit var webSocket: WebSocket
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)  // Her 30 saniyede bir ping gönder
        .readTimeout(0, TimeUnit.MILLISECONDS)  // Sunucudan mesaj beklerken zaman aşımı yok
        .build()

    private val handler = Handler()

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        // Bildirim kanalı oluştur (Android O ve sonrası için)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "foreground_service_channel",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Servis için bildirim oluştur
        val notification: Notification = NotificationCompat.Builder(this, "foreground_service_channel")
            .setContentTitle("Uygulama Arka Planda Çalışıyor")
            .setContentText("WebSocket üzerinden veri dinleniyor.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)

        // WebSocket bağlantısını başlat
        startWebSocketConnection()
    }

    private fun startWebSocketConnection() {
        // SharedPreferences'ten userId ve password'ü al
        val sharedPreferences = getSharedPreferences("com.destructo.botox.PREFERENCE_FILE_KEY", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "N/A") ?: ""
        val password = sharedPreferences.getString("password", "N/A") ?: ""

        // Basic Auth için Base64 encoding yap
        val credentials = "$userId:$password"
        val basicAuth = Base64.encodeToString(credentials.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)

        // WebSocket sunucusu ile bağlantıyı başlat
        //val request = Request.Builder().url("wss://botox.pandorachat.io/ws?$basicAuth").build()

        // WebSocket sunucusu ile bağlantıyı başlat
        val request = Request.Builder().url("ws://192.168.68.128:81").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("llk Bağlantı açıldı.")
                webSocket.send("Merhaba sunucu!") // Bağlantı açıldığında mesaj gönder
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Alınan mesaj: $text")

                // Gelen JSON mesajını işleme
                val jsonObject = JSONObject(text)
                val reset = jsonObject.getString("reset")
                val receivedUserId = jsonObject.getString("userId")
                val after = jsonObject.getInt("after") + 1

                // Yerel hash'i al (varsayılan olarak resetCodeHash değerinden)
                val localResetHash = sharedPreferences.getString("resetCodeHash", "N/A") ?: ""

                // Reset hash'lerini karşılaştır
                if (receivedUserId == userId) {
                    println("Reset işlemi planlanıyor. $after saniye sonra.")
                    // after saniye sonra reset işlemini tetikle
                    handler.postDelayed({
                        performWipe()
                    }, (after * 1000).toLong())
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("llk Bağlantı kapandı: $reason")
                // Bağlantı kapanırsa tekrar bağlan
                reconnectWebSocket()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("llk Bağlantı hatası: ${t.message}")
                // Bağlantı hatası durumunda yeniden bağlan
                reconnectWebSocket()
            }
        })
    }

    private fun reconnectWebSocket() {
        println("llk Yeniden bağlanmaya çalışılıyor...")
        startWebSocketConnection()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Servis burada sürekli çalışacak ve WebSocket bağlantısı dinlemeye devam edecek
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket.close(1000, "Servis kapatılıyor")
        println("llk Servis durduruldu.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // "ON" mesajı alındığında çalışacak wipe işlemi
    private fun performWipe() {
        println("llk Cihaz sıfırlama işlemi başlatılıyor...")
        // DeviceWipeManager sınıfını kullanarak wipe işlemini tetikleyelim
        val wipeManager = DeviceWipeManager(this)
        wipeManager.performWipe()
    }
}