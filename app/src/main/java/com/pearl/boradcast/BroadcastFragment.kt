package com.pearl.boradcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class BroadcastFragment : Fragment(R.layout.fragment_broadcast) {

    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var receivedBroadcastsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val message = intent?.getStringExtra("message") ?: "No message"
                Log.d("BroadcastReceiver", "Received: $message")
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                receivedBroadcastsTextView.append("$message\n")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("BroadcastFragment", "onStart: Registering broadcast receiver")
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, IntentFilter("custom-event-name"))
    }

    override fun onStop() {
        Log.d("BroadcastFragment", "onStop: Unregistering broadcast receiver")
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receivedBroadcastsTextView = view.findViewById(R.id.received_broadcasts)

        // Set up the button to send a local broadcast
        val broadcastButton: Button = view.findViewById(R.id.broadcast_button)
        broadcastButton.setOnClickListener {
            sendLocalBroadcast("Hello from BroadcastFragment!")
        }

        // Set up the button to send a system broadcast
        val systemBroadcastButton: Button = view.findViewById(R.id.system_broadcast_button)
        systemBroadcastButton.setOnClickListener {
            sendSystemBroadcast("Hello from BroadcastFragment - System Broadcast!")
        }
    }

    private fun sendLocalBroadcast(message: String) {
        val intent = Intent("custom-event-name")
        intent.putExtra("message", message)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        Log.d("BroadcastFragment", "Sent local broadcast with message: $message")
    }

    private fun sendSystemBroadcast(message: String) {
        val intent = Intent("com.example.snippets.ACTION_UPDATE_DATA").apply {
            putExtra("com.example.snippets.DATA", message)
        }
        context?.sendBroadcast(intent)
        Log.d("BroadcastFragment", "Sent system broadcast with action: ${intent.action} and message: $message")
    }
}