//package com.pearl.broadcast
//
//import android.app.DownloadManager
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Bundle
//import android.os.Environment
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import com.pearl.boradcast.R
//
//class BroadcastFragment : Fragment() {
//
//    private lateinit var downloadButton: Button
//    private lateinit var receivedBroadcasts: TextView
//    private lateinit var downloadManager: DownloadManager
//
//    private val REQUEST_CODE = 100
//    private val DOWNLOAD_COMPLETE_ACTION = "com.pearl.broadcast.DOWNLOAD_COMPLETE"
//
//    private val downloadReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
//            Log.d("Download", "Download completed with ID: $id") // Log download ID
//            Toast.makeText(context, "Download completed: $id", Toast.LENGTH_SHORT).show()
//            receivedBroadcasts.text = "Download Completed: $id"
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_broadcast, container, false)
//
//        downloadButton = view.findViewById(R.id.download_button)
//        receivedBroadcasts = view.findViewById(R.id.received_broadcasts)
//
//        checkPermissions()
//
//        downloadButton.setOnClickListener {
//            startDownload("https://wallpapercave.com/wp/wp2035789.jpg") // Replace with actual file URL
//        }
//
//        return view
//    }
//
//    private fun startDownload(url: String) {
//        downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        val request = DownloadManager.Request(Uri.parse(url))
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "downloaded_file.jpg")
//
//        val downloadId = downloadManager.enqueue(request)
//        Log.d("Download", "Download started with ID: $downloadId")
//
//        // Send a local broadcast for download completion (for Android 14)
//        val intent = Intent(DOWNLOAD_COMPLETE_ACTION)
//        intent.setPackage(requireContext().packageName) // Required for Android 14+
//        requireContext().sendBroadcast(intent)
//    }
//
//    private fun checkPermissions() {
//        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                REQUEST_CODE
//            )
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        val intentFilter = IntentFilter(DOWNLOAD_COMPLETE_ACTION) // Custom action
//        requireContext().registerReceiver(downloadReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        requireContext().unregisterReceiver(downloadReceiver)
//    }
//}
//
//


package com.pearl.broadcast

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.pearl.boradcast.R

class BroadcastFragment : Fragment() {

    private lateinit var downloadButton: Button
    private lateinit var receivedBroadcasts: TextView
    private lateinit var downloadManager: DownloadManager

    private val REQUEST_CODE = 100
    private val DOWNLOAD_COMPLETE_ACTION = "android.intent.action.DOWNLOAD_COMPLETE"

    private val downloadIds = mutableSetOf<Long>() // Store multiple download IDs

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return

            if (downloadIds.contains(id)) { // Check if it's our download
                Log.d("Download", "Download completed with ID: $id")
                Toast.makeText(context, "Download completed: $id", Toast.LENGTH_SHORT).show()
                receivedBroadcasts.text = "Download Completed: $id"
                downloadIds.remove(id) // Remove after completion
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_broadcast, container, false)

        downloadButton = view.findViewById(R.id.download_button)
        receivedBroadcasts = view.findViewById(R.id.received_broadcasts)

        checkPermissions()

        downloadButton.setOnClickListener {
            startDownload("https://wallpapercave.com/wp/wp2035789.jpg") // Replace with actual file URL
        }

        return view
    }

    private fun startDownload(url: String) {
        downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloaded_file_${System.currentTimeMillis()}.jpg")

        val downloadId = downloadManager.enqueue(request) // Store unique download ID
        downloadIds.add(downloadId)

        Log.d("Download", "Download started with ID: $downloadId")
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(DOWNLOAD_COMPLETE_ACTION) // System action
        requireContext().registerReceiver(downloadReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(downloadReceiver)
    }
}
