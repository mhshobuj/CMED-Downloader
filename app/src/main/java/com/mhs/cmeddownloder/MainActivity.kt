package com.mhs.cmeddownloder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var startDownload: Button
    private lateinit var txtValue: TextView
    private var downloadNotification: DownloadNotification? = null
    private var progress: Int = 0
    private var status: Boolean = false

    val STORAGE_DIRECTORY = Constants.FILE_DIRECTORY
    val fileName = System.currentTimeMillis().toString().replace(":", ".")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        startDownload = findViewById(R.id.startDownload)
        txtValue = findViewById(R.id.txtValue)

        // Create an instance of DownloadNotification
        downloadNotification = DownloadNotification(this)

        // Start download on button click
        startDownload.setOnClickListener {
            downloadFile(Constants.FILE_URL, fileName)
        }
    }


    private fun downloadFile(mUrl: String?, fileName: String) {
        // Implement download logic
        val storageDirectory = getExternalFilesDir("/")?.absolutePath + STORAGE_DIRECTORY + "/${fileName}"
        val file = File(getExternalFilesDir("/")?.absolutePath + STORAGE_DIRECTORY)

        if (!file.exists()) {
            file.mkdirs()
        }

        //for global score this download process also continue when apps goes to background
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(mUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept-Encoding", "identity")
            connection.content

            if (connection.responseCode in 200..299) {
                val fileSize = connection.contentLength
                val inputStream = connection.inputStream

                val outputStream = FileOutputStream(storageDirectory)

                var bytesCopied: Long = 0
                val buffer = ByteArray(1024)
                var bytes = inputStream.read(buffer)
                while (bytes >= 0) {
                    bytesCopied += bytes
                    progress = ((bytesCopied.toFloat() / fileSize.toFloat()) * 100).toInt()
                    // Update the notification with the current progress
                    withContext(Dispatchers.Main) {
                        progressBar.progress = progress
                        txtValue.text = "$progress%"
                        if (status){
                            downloadNotification?.showNotification(progress)
                        }
                    }
                    outputStream.write(buffer, 0, bytes)
                    bytes = inputStream.read(buffer)
                }
                outputStream.close()
                inputStream.close()

                progressBar.progress = 0
                txtValue.text = "0"

                if (status){
                    downloadNotification?.cancelNotification()
                }
                //called this method after complete the download, user gets a notification of download complete
                showDownloadCompleteNotification()
            } else {
                Toast.makeText(this@MainActivity, "Not success", Toast.LENGTH_LONG).show()
            }
        }
    }

    //download notification start when apps goes to background
    override fun onPause() {
        super.onPause()
        status = true
        downloadNotification?.showNotification(progress)
    }

    //download notification close when apps destroy
    override fun onDestroy() {
        super.onDestroy()
        status = false
        downloadNotification?.cancelNotification()
    }

    //download notification cancel when user again open apps from background
    override fun onResume() {
        super.onResume()
        status = false
        downloadNotification?.cancelNotification()
    }

    private fun showDownloadCompleteNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification
        val notificationBuilder = NotificationCompat.Builder(this, "channelId")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Download Complete")
            .setContentText("File downloaded successfully: $fileName.mp3")
            .setAutoCancel(true)

        // Show the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("channelId", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(1, notificationBuilder.build())
    }
}