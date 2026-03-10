package com.huawei.voiceagent

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VoiceAgentApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // 初始化HMS Core
        initHmsCore()
    }
    
    private fun initHmsCore() {
        try {
            // HMS Core会自动初始化
            // 如果需要手动初始化，可以在这里添加代码
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}