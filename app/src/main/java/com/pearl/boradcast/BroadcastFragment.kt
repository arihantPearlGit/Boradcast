package com.pearl.boradcast

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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class BroadcastFragment : Fragment() {

    private lateinit var downloadButton: Button
    private lateinit var receivedBroadcasts: TextView
    private lateinit var downloadManager: DownloadManager
    private lateinit var downloadReceiver: BroadcastReceiver

    private val REQUEST_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_broadcast, container, false)

        downloadButton = view.findViewById(R.id.download_button)
        receivedBroadcasts = view.findViewById(R.id.received_broadcasts)

        checkPermissions()

        downloadButton.setOnClickListener {
            startDownload("https://wallpapercave.com/wp/wp2035789.jpg") // Replace with your file URL
        }

        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                // Handle download completion
                Toast.makeText(context, "Download completed: $id", Toast.LENGTH_SHORT).show()
                Log.d("BroadcastFragment", "Download completed with ID: $id")

                // Open the directory where the file is saved
                val fileUri = Uri.parse("file://${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/downloaded_file.jpg")
                val openFileIntent = Intent(Intent.ACTION_VIEW)
                openFileIntent.setDataAndType(fileUri, "image/jpeg")
                openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(openFileIntent)
            }
        }

        return view
    }

    private fun startDownload(url: String) {
        downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "downloaded_file.jpg") // Change the filename as needed
        val downloadId = downloadManager.enqueue(request)
        Log.d("Download", "Download started with ID: $downloadId")
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
        ContextCompat.registerReceiver(
            requireContext(),
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(downloadReceiver)
    }
}