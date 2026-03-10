package com.huawei.voiceagent.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.voiceagent.R
import com.huawei.voiceagent.databinding.ActivityMainBinding
import com.huawei.voiceagent.ui.adapter.ChatAdapter
import com.huawei.voiceagent.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    private var ttsEngine: TextToSpeech? = null

    // 权限请求
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "麦克风权限已授予", Toast.LENGTH_SHORT).show()
            initVoiceComponents()
        } else {
            Toast.makeText(this, "需要麦克风权限才能使用语音功能", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        checkPermissions()
    }

    private fun setupUI() {
        // 设置Toolbar
        setSupportActionBar(binding.toolbar)

        // 设置RecyclerView
        chatAdapter = ChatAdapter()
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = chatAdapter
        }

        // 设置按钮点击事件
        binding.fabMic.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.toggleVoiceRecording()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etInput.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendTextMessage(text)
                binding.etInput.text?.clear()
            }
        }

        binding.etInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                val text = binding.etInput.text.toString().trim()
                if (text.isNotEmpty()) {
                    viewModel.sendTextMessage(text)
                    binding.etInput.text?.clear()
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupObservers() {
        viewModel.chatMessages.observe(this) { messages ->
            chatAdapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.rvChat.smoothScrollToPosition(messages.size - 1)
            }
        }

        viewModel.status.observe(this) { status ->
            binding.tvStatus.text = status
        }

        viewModel.isRecording.observe(this) { isRecording ->
            val colorRes = if (isRecording) R.color.red_500 else R.color.purple_500
            binding.fabMic.setBackgroundColor(ContextCompat.getColor(this, colorRes))
        }

        viewModel.ttsText.observe(this) { text ->
            if (text.isNotEmpty()) {
                speakText(text)
            }
        }
    }

    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                initVoiceComponents()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(
                    this,
                    "需要麦克风权限来进行语音识别",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun initVoiceComponents() {
        // 初始化TTS
        ttsEngine = TextToSpeech(this, this)
        
        // 初始化语音识别（模拟模式）
        viewModel.initSpeechRecognizer(this)
    }

    private fun speakText(text: String) {
        ttsEngine?.let { tts ->
            if (tts.isSpeaking) {
                tts.stop()
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = ttsEngine?.setLanguage(Locale.CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Log.e("TTS", "中文语言包不可用")
            } else {
                Log.d("TTS", "TTS引擎初始化成功")
            }
        } else {
            Log.e("TTS", "TTS引擎初始化失败")
        }
    }

    override fun onDestroy() {
        ttsEngine?.stop()
        ttsEngine?.shutdown()
        viewModel.release()
        super.onDestroy()
    }
}