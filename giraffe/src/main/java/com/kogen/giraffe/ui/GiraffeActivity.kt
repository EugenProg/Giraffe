package com.kogen.giraffe.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.kogen.giraffe.navigation.AppNavHost
import kz.evko.navigation.annotation.KoGenScreen

@SuppressLint("RestrictedApi")
class GiraffeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatId = intent?.getStringExtra("EXTRA_CHAT_ID")
        if (chatId != null) {
            handleChatSelection(chatId)
        }
        setContent {
            MaterialTheme {
                AppNavHost(navController = rememberNavController())
            }
        }
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

@KoGenScreen(startDestination = true)
@Composable
fun ChatListScreen() {
    Scaffold(
        containerColor = Color.Cyan,
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Text("Hello Giraffe!!!", color = Color.White)
        }
    }
}