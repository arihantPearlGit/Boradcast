package com.pearl.boradcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("com.example.snippets.DATA") ?: "No message"
        Log.d("MyBroadcastReceiver", "Received system broadcast: $message")
        Toast.makeText(context, "System Broadcast: $message", Toast.LENGTH_SHORT).show()
    }
}