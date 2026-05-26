package com.arick.aiassistant.core.network

object NetworkConfig {
    // Physical devices on the same LAN can reach the local backend through the Mac's IP.
    const val DEFAULT_BASE_URL: String = "http://127.0.0.1:8000/"
}
