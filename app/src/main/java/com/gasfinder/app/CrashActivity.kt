package com.gasfinder.app

import android.app.Activity
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView

class CrashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trace = intent.getStringExtra("crash_trace") ?: "Unknown crash"

        val textView = TextView(this).apply {
            text = trace
            textSize = 12f
            setPadding(24, 24, 24, 24)
            setTextIsSelectable(true)
        }
        val scrollView = ScrollView(this).apply {
            addView(textView)
        }
        setContentView(scrollView)
    }
}
