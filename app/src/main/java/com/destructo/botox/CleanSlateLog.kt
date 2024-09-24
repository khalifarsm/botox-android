package com.destructo.botox

object CleanSlateLog {

    // General log function
    fun log(message: String) {
        // Check if logging is enabled
        if (Configuration.ENABLE_LOGS) {
            println(Configuration.APP_NAME + "Log: $message")
        }
    }
}