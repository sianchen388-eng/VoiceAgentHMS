package com.huawei.voiceagent.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huawei.voiceagent.data.model.ChatMessage
import com.huawei.voiceagent.data.model.MessageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _status = MutableLiveData<String>("准备就绪")
    val status: LiveData<String> = _status

    private val _isRecording = MutableLiveData<Boolean>(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _ttsText = MutableLiveData<String>("")
    val ttsText: LiveData<String> = _ttsText

    private val chatList = mutableListOf<ChatMessage>()

    init {
        // 添加欢迎消息
        addMessage(ChatMessage(
            text = "你好！我是语音助手，请点击麦克风按钮开始说话。",
            type = MessageType.BOT,
            timestamp = System.currentTimeMillis()
        ))
    }

    fun initSpeechRecognizer(context: Context) {
        // 简化版本，不实际初始化HMS
        _status.value = "语音服务模拟模式"
    }

    fun toggleVoiceRecording() {
        if (_isRecording.value == true) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        viewModelScope.launch {
            _isRecording.value = true
            _status.value = "正在聆听...（模拟模式）"
            
            // 模拟录音3秒
            delay(3000)
            
            // 模拟识别结果
            val simulatedText = "你好，现在几点了？"
            addMessage(ChatMessage(
                text = simulatedText,
                type = MessageType.USER,
                timestamp = System.currentTimeMillis()
            ))
            
            _isRecording.value = false
            _status.value = "识别完成"
            
            // 处理用户输入
            processUserInput(simulatedText)
        }
    }

    private fun stopRecording() {
        _isRecording.value = false
        _status.value = "已停止录音"
    }

    fun sendTextMessage(text: String) {
        if (text.isNotEmpty()) {
            addMessage(ChatMessage(
                text = text,
                type = MessageType.USER,
                timestamp = System.currentTimeMillis()
            ))
            processUserInput(text)
        }
    }

    private fun processUserInput(input: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // 模拟AI响应
                val response = when {
                    input.contains("你好") || input.contains("hi") || input.contains("hello") -> 
                        "你好！我是语音助手，很高兴为您服务。"
                    input.contains("时间") -> 
                        "现在时间是：${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}"
                    input.contains("天气") -> 
                        "今天天气晴朗，温度适宜，适合外出。"
                    input.contains("谢谢") -> 
                        "不客气！有什么其他可以帮您的吗？"
                    else -> 
                        "我听到您说：\"$input\"。这是一个很好的问题，但我目前还在学习中。"
                }
                
                // 添加AI响应
                addMessage(ChatMessage(
                    text = response,
                    type = MessageType.BOT,
                    timestamp = System.currentTimeMillis()
                ))
                
                // 触发TTS播放（模拟）
                _ttsText.postValue(response)
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        chatList.add(message)
        _chatMessages.postValue(chatList.toList())
    }

    fun release() {
        // 简化版本，无需释放资源
    }
}