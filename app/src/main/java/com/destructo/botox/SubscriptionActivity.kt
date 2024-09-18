package com.destructo.botox

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class SubscriptionActivity : AppCompatActivity() {

    private lateinit var etSubscriptionCode: EditText
    private lateinit var etResetCode: EditText
    private lateinit var etResetCodeConfirm: EditText
    private lateinit var btnSubmit: Button
    private lateinit var apiService: ApiService
    private var fcmToken: String? = null // FCM token için değişken tanımla

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Shared Preferences kontrolü, eğer userId varsa UserIDActivity'ye geç
        val sharedPreferences = getSharedPreferences("com.destructo.botox.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)
        if (userId != null) {
            // Kullanıcı daha önce kayıt olmuş, UserIDActivity'yi başlat
            val intent = Intent(this, UserIDActivity::class.java)
            startActivity(intent)
            finish() // Bu activity'yi kapat
            return
        }

        setContentView(R.layout.activity_subscription)

        // Retrofit'i başlat
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://botox.pandorachat.io/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Görselleri tanımla
        etSubscriptionCode = findViewById(R.id.et_subscription_code)
        etResetCode = findViewById(R.id.et_reset_code)
        etResetCodeConfirm = findViewById(R.id.et_reset_code_confirm)
        btnSubmit = findViewById(R.id.btn_submit)

        // FCM token'ı al
        askNotificationPermissionAndGetToken()

        btnSubmit.setOnClickListener {
            val subscriptionCode = etSubscriptionCode.text.toString().trim()
            val resetCode = etResetCode.text.toString().trim()
            val resetCodeConfirm = etResetCodeConfirm.text.toString().trim()

            if (validateInputs(subscriptionCode, resetCode, resetCodeConfirm)) {
                val randomPassword = UUID.randomUUID().toString()
                val resetCodeHash = generateHash(resetCode)

                // Eğer token hala alınmadıysa işlem yapma
                if (fcmToken != null) {
                    // FCM token'ı ile birlikte kullanıcıyı kaydet
                    registerUser(subscriptionCode, resetCodeHash, randomPassword, fcmToken!!)
                } else {
                    Toast.makeText(this, "FCM Token alınamadı, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // İzin talebi başlat
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            getFcmToken()
        } else {
            Toast.makeText(this, "Bildirim izni verilmedi, bildirimler gösterilmeyecek.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askNotificationPermissionAndGetToken() {
        // Android 13 (API 33) ve üzeri sürümlerde izin gerekli
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // İzin verilmişse, FCM token'ı al
                getFcmToken()
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // Kullanıcıya izin gerekliliğini açıkla (Eğitici UI gibi)
                // Örneğin kullanıcıya neden iznin gerekli olduğunu anlatan bir mesaj göster
                // Daha sonra requestPermissionLauncher ile izni iste
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Direkt izni iste
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 13'ten önceki sürümler için doğrudan FCM token'ı al
            getFcmToken()
        }
    }

    // Firebase Cloud Messaging token'ı al
    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("SubscriptionActivity", "FCM token alma işlemi başarısız oldu", task.exception)
                return@OnCompleteListener
            }

            // FCM token alındı
            fcmToken = task.result
            Log.d("SubscriptionActivity", "FCM Token: $fcmToken")
        })
    }

    private fun validateInputs(subscriptionCode: String, resetCode: String, resetCodeConfirm: String): Boolean {
        if (subscriptionCode.isEmpty() || resetCode.isEmpty() || resetCodeConfirm.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (resetCode != resetCodeConfirm) {
            Toast.makeText(this, "Reset codes do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun generateHash(resetCode: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(resetCode.toByteArray())
            val hexString = StringBuilder()
            for (b in hash) {
                val hex = Integer.toHexString(0xff and b.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            ""
        }
    }

    private fun registerUser(subscriptionCode: String, resetCodeHash: String, password: String, fcmToken: String) {
        val requestBody = JsonObject().apply {
            addProperty("subscription", subscriptionCode)
            addProperty("reset", resetCodeHash)
            addProperty("password", password)
            addProperty("token", fcmToken)  // Gerçek FCM token'ı kullan
        }

        val call = apiService.registerUser(requestBody)
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val userId = responseBody?.get("userId")?.asString

                    saveToSharedPreferences(subscriptionCode, resetCodeHash, password, fcmToken, userId)

                    // UserIDActivity'yi başlat
                    val intent = Intent(this@SubscriptionActivity, UserIDActivity::class.java)
                    startActivity(intent)
                    finish() // SubscriptionActivity'yi kapat
                } else {
                    Toast.makeText(this@SubscriptionActivity, "API Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@SubscriptionActivity, "API Call Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveToSharedPreferences(subscriptionCode: String, resetCodeHash: String, password: String, fcmToken: String, userId: String?) {
        val sharedPreferences = getSharedPreferences("com.destructo.botox.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("subscriptionCode", subscriptionCode)
            putString("resetCodeHash", resetCodeHash)
            putString("password", password)
            putString("fcmToken", fcmToken)
            putString("userId", userId)
            apply()
        }
    }

    interface ApiService {
        @Headers("Content-Type: application/json")
        @POST("register")
        fun registerUser(@Body requestBody: JsonObject): Call<JsonObject>
    }
}