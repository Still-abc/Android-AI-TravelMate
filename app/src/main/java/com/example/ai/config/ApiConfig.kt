package com.example.ai.config

object ApiConfig {
    //==================================================
    // TODO[Developer Fill]
    // Generic network configuration
    //==================================================
    const val DEFAULT_BASE_URL = "https://localhost/"

    //==================================================
    // TODO[Developer Fill]
    // AI large model configuration
    //==================================================
    const val AI_BASE_URL = "你的AI服务地址"
    const val AI_API_KEY = "你的AI密钥"
    const val AI_MODEL = "你的AI模型名称"
    const val AI_TEMPERATURE = 0.7f
    const val AI_MAX_TOKENS = 4096

    //==================================================
    // TODO[Developer Fill]
    // Weather API configuration
    //==================================================
    const val WEATHER_API_KEY = "你的密钥"
    const val WEATHER_BASE_URL = "你的天气服务地址"

    //==================================================
    // TODO[Developer Fill]
    // AMap Web Service configuration
    //==================================================
    const val AMAP_WEB_KEY = "你的密钥"
    const val AMAP_BASE_URL = "你的地图服务地址"

    //==================================================
    // TODO[Developer Fill]
    // Pexels image API configuration
    //==================================================
    const val PEXELS_API_KEY = "你的密钥"
    const val PEXELS_BASE_URL = "你的图片服务地址"


    private fun isConfigured(value: String): Boolean =
        value.isNotBlank() && !value.startsWith("YOUR_")

    fun hasRuntimeAIConfig(): Boolean =
        isConfigured(AI_BASE_URL) && isConfigured(AI_API_KEY) && AI_MODEL.isNotBlank()

    fun hasValidAIConfig(): Boolean = hasRuntimeAIConfig()

    fun hasWeatherConfig(): Boolean = isConfigured(WEATHER_API_KEY)
    fun hasAmapConfig(): Boolean = isConfigured(AMAP_WEB_KEY)
    fun hasPexelsConfig(): Boolean = isConfigured(PEXELS_API_KEY)
}
