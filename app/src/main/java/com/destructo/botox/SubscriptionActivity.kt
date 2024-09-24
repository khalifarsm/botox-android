package com.destructo.botox

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
import com.google.gson.JsonParser
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
import kotlin.reflect.KMutableProperty0


class SubscriptionActivity : AppCompatActivity() {

    private lateinit var etSubscriptionCode: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private lateinit var btnSubmit: Button
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Shared Preferences check, if userId exists, proceed to UserIDActivity
        val sharedPreferences = getSharedPreferences("com.destructo.botox.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)
        askNotificationPermission()
        if (userId != null) {
            // The user has already registered, start UserIDActivity
            val intent = Intent(this, UserIDActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_subscription)

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        val url = Configuration.API_URL
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        apiService = retrofit.create(ApiService::class.java)

        etSubscriptionCode = findViewById(R.id.et_subscription_code)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnSubmit = findViewById(R.id.btn_submit)

        setupPasswordVisibilityToggle(etPassword, ::isPasswordVisible)
        setupPasswordVisibilityToggle(etConfirmPassword, ::isConfirmPasswordVisible)
        setEyeIconDefaultColor(etPassword)
        setEyeIconDefaultColor(etConfirmPassword)


        btnSubmit.setOnClickListener {
            val subscriptionCode = etSubscriptionCode.text.toString().trim()
            val resetCode = etPassword.text.toString().trim()
            val resetCodeConfirm = etConfirmPassword.text.toString().trim()

            if (validateInputs(subscriptionCode, resetCode, resetCodeConfirm)) {
                val randomPassword = UUID.randomUUID().toString()
                val resetCodeHash = generateHash(resetCode)
                registerUser(subscriptionCode, resetCodeHash, randomPassword,)

            }
        }
    }


    private fun setEyeIconDefaultColor(editText: EditText) {
        val eyeIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_view)
        eyeIcon?.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray), PorterDuff.Mode.SRC_IN)
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, eyeIcon, null)
    }

    private fun setupPasswordVisibilityToggle(editText: EditText, isVisible: KMutableProperty0<Boolean>) {
        editText.setOnTouchListener { _, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (editText.right - editText.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    togglePasswordVisibility(editText, isVisible)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun togglePasswordVisibility(editText: EditText, isVisible: KMutableProperty0<Boolean>) {

        val currentTypeface = editText.typeface
        if (isVisible.get()) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            val eyeIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_view)
            eyeIcon?.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray), PorterDuff.Mode.SRC_IN)
            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, eyeIcon, null)
            editText.typeface = currentTypeface
            isVisible.set(false)
        } else {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            val eyeIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_view)
            eyeIcon?.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_blue_light), PorterDuff.Mode.SRC_IN)
            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, eyeIcon, null)
            editText.typeface = currentTypeface
            isVisible.set(true)
        }
        editText.setSelection(editText.text.length)
    }


    // Request permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Proceed with the foreground service notification
        } else {
        }
    }

    private fun askNotificationPermission() {
        // Android 13 (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted, proceed with notifications
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // Explain to the user why this permission is necessary
                // Example: Show a message explaining why notifications are important for the app
                // For instance, you can show a dialog or UI explaining that notifications keep them updated on important background processes.
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly request the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // No need to request permission on devices below Android 13 (API 33)
        }
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
        return HashUtil.generateHash(resetCode)
    }

    private fun registerUser(subscriptionCode: String, resetCodeHash: String, password: String) {
        val requestBody = JsonObject().apply {
            addProperty("subscription", subscriptionCode)
            addProperty("reset", resetCodeHash)
            addProperty("password", password)
        }

        val call = apiService.registerUser(requestBody)
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val userId = responseBody?.get("userId")?.asString

                    saveToSharedPreferences(subscriptionCode, resetCodeHash, password, userId)

                    val intent = Intent(this@SubscriptionActivity, UserIDActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // API error, error body'yi işleyelim
                    try {
                        val errorBody = response.errorBody()?.string()
                        val jsonError = JsonParser.parseString(errorBody).asJsonObject
                        val errorMessage = jsonError.get("message").asString
                        Toast.makeText(this@SubscriptionActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // Hata JSON formatında değilse veya başka bir hata oluşursa, genel bir hata mesajı göster
                        Toast.makeText(this@SubscriptionActivity, "${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@SubscriptionActivity, "An error occurred, please try again later.", Toast.LENGTH_SHORT).show()
            }
        })
    }

private fun saveToSharedPreferences(subscriptionCode: String, resetCodeHash: String, password: String, userId: String?) {
    val sharedPreferences = getSharedPreferences("com.destructo.botox.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("subscriptionCode", subscriptionCode)
        putString("resetCodeHash", resetCodeHash)
        putString("password", password)
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