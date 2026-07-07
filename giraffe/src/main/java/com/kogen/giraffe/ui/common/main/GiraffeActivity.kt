package com.kogen.giraffe.ui.common.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.kogen.giraffe.di.setApplicationContext
import com.kogen.giraffe.navigation.ActionToChatDetailsContainer
import com.kogen.giraffe.navigation.AppNavHost
import com.kogen.giraffe.navigation.navigateSafety
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@SuppressLint("RestrictedApi")
class GiraffeActivity : ComponentActivity() {
    private val _pendingChatId = MutableSharedFlow<String>(replay = 1)
    private val pendingChatId = _pendingChatId.asSharedFlow()

    private fun navigateTo(chatId: String) {
        _pendingChatId.tryEmit(chatId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApplicationContext(this.applicationContext)
        enableEdgeToEdge(
            statusBarStyle = getStatusBarStyle(),
            navigationBarStyle = getStatusBarStyle(),
        )
        setContent {
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                pendingChatId.collect { chatId ->
                    navController.navigateSafety(ActionToChatDetailsContainer(chatId))
                }
            }

            AppNavHost(navController = navController)
        }
        intent?.getStringExtra("EXTRA_CHAT_ID")?.let {
            navigateTo(it)
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

        intent.getStringExtra("EXTRA_CHAT_ID")?.let {
            navigateTo(it)
        }
    }
}