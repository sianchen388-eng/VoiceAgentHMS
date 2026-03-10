package com.huawei.voiceagent.data.model

import java.util.Date

enum class MessageType {
    USER, BOT
}

data class ChatMessage(
    val text: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() = java.text.SimpleDateFormat("HH:mm").format(Date(timestamp))
    
    val isUserMessage: Boolean
        get() = type == MessageType.USER
}