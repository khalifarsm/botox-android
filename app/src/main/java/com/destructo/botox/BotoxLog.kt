package com.destructo.botox

object BotoxLog {

    // General log function
    fun log(message: String) {
        // Check if logging is enabled
        if (Configuration.ENABLE_LOGS) {
            println("BotoxLog: $message")
        }
    }
}