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
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
    private lateinit var webView: WebView
    private lateinit var downloadManager: DownloadManager

    private val REQUEST_CODE = 100
    private val DOWNLOAD_COMPLETE_ACTION = "android.intent.action.DOWNLOAD_COMPLETE"

    private val downloadIds = mutableSetOf<Long>()

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return

            if (downloadIds.contains(id)) {
                Log.d("Download", "Download completed with ID: $id")
                Toast.makeText(context, "Download completed: $id", Toast.LENGTH_SHORT).show()
                receivedBroadcasts.text = "Download Completed: $id"
                downloadIds.remove(id)

                // Redirect to the download location
                openDownloadedFile(context, id)
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
        webView = view.findViewById(R.id.webview)

        checkPermissions()

        // Set up WebView
        setupWebView()

        downloadButton.setOnClickListener {
            downloadPdfFromLink("https://drive.google.com/uc?id=0B5KPf48VyX8WUGlhbVRTMVRHXzA")
        }

        return view
    }

    private fun setupWebView() {
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true

        // Load a URL in the WebView
        webView.loadUrl("https://erp.pearlorganisation.in/") // Replace with your desired URL

        // Handle file downloads
        webView.setDownloadListener(object : DownloadListener {
            override fun onDownloadStart(url: String?, userAgent: String?, contentDisposition: String?, mimeType: String?, contentLength: Long) {
                if (url != null) {
                    startDownload(url, "pdf") // You can modify the file type based on the URL
                }
            }
        })
    }

    private fun downloadPdfFromLink(link: String) {
        startDownload(link, "pdf")
        Log.d("Download", "Download started with $link")
    }

    private fun startDownload(url: String, fileType: String) {
        downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        // Set the destination file name based on the file type
        val fileName = "downloaded_file_${System.currentTimeMillis()}.$fileType"
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadId = downloadManager.enqueue(request)
        downloadIds.add(downloadId)

        Log.d("Download", "Download started with ID: $downloadId")
        Toast.makeText(requireContext(), "Download started: $downloadId", Toast.LENGTH_SHORT).show()
    }

    private fun openDownloadedFile(context: Context?, downloadId: Long) {
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            val localUri = cursor.getString(columnIndex)

            // Open the file location
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(localUri), "application/pdf")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(intent)
        }
        cursor.close()
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