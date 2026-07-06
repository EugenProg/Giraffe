package com.kogen.giraffe.ui.common.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.kogen.giraffe.di.setApplicationContext
import com.kogen.giraffe.navigation.AppNavHost

@SuppressLint("RestrictedApi")
class GiraffeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApplicationContext(this.applicationContext)
        val chatId = intent?.getStringExtra("EXTRA_CHAT_ID")
        if (chatId != null) {
            handleChatSelection(chatId)
        }
        enableEdgeToEdge(
            statusBarStyle = getStatusBarStyle(),
            navigationBarStyle = getStatusBarStyle(),
        )
        setContent {
            AppNavHost(navController = rememberNavController())
        }
    }

    private fun getStatusBarStyle(): SystemBarStyle {
        val lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
        return SystemBarStyle.auto(
            lightScrim = lightScrim,
            darkScrim = darkScrim,
            detectDarkMode = {
                true
            },
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val newChatId = intent.getStringExtra("EXTRA_CHAT_ID")
        if (newChatId != null) {
            handleChatSelection(newChatId)
        }
    }

    private fun handleChatSelection(chatId: String) {
        Toast.makeText(this, "ChatId: $chatId", Toast.LENGTH_SHORT).show()
    }
}