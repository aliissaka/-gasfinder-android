package com.gasfinder.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.gasfinder.app.network.TokenManager
import com.gasfinder.app.ui.GasFinderNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val intent = Intent(applicationContext, CrashActivity::class.java).apply {
                    putExtra("crash_trace", throwable.stackTraceToString())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                applicationContext.startActivity(intent)
            } catch (e: Exception) {
                // fall through to default handler if we can't even show the crash screen
            }
            Runtime.getRuntime().exit(1)
        }

        TokenManager.init(applicationContext)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GasFinderNavGraph()
                }
            }
        }
    }
}
