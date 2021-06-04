package com.example.workmanagerexample

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.work.*
import com.example.workmanagerexample.EmotionAnalysisWorker.Companion.KEY_USER_EMOTION_RESULT
import com.example.workmanagerexample.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    companion object {
        val KEY_USER_COMMENT_TEXT = "key.user.comment.text"
        val KEY_DOWNLOAD_TITLE = "key.download.title"
        val KEY_DOWNLOAD_DESC = "key.download.desc"
        val KEY_UPLOAD_TITLE = "key.upload.title"
        val KEY_UPLOAD_DESC = "key.upload.desc"
        val TAG_SEND_LOG = "tag.send.log"
        val CHANNEL_ID = "4747"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        createNotificationChannel()

        binding.buttonEmotionAnalysis.setOnClickListener {
            val userText = binding.editTextUserComment.text?.toString() ?: ""
            setOneTimeEmotionAnalysisRequest(userText)
        }

        binding.buttonRaconFilter.setOnClickListener {
            setOneTimeGoldenRatioFilterRequest()
        }

        binding.switch1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                setPeriodicallySendingLogs()
            } else {
                cancellSendingLogRequest()
            }
        }

    }

    private fun setOneTimeEmotionAnalysisRequest(userText: String) {

        val workManager = WorkManager.getInstance(this)

        val data = Data.Builder()
            .putString(KEY_USER_COMMENT_TEXT, userText)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val emotionAnalysisWorker = OneTimeWorkRequestBuilder<EmotionAnalysisWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .build()

        workManager.enqueue(emotionAnalysisWorker)

        workManager.getWorkInfoByIdLiveData(emotionAnalysisWorker.id)
            .observe(this, Observer { workInfo ->
                binding.textViewWorkState.text = workInfo.state.name

                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    val userEmotionResult = workInfo.outputData.getString(KEY_USER_EMOTION_RESULT)
                    Toast.makeText(this, userEmotionResult, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setOneTimeGoldenRatioFilterRequest() {

        val workManager = WorkManager.getInstance(this)

        val downloadData = workDataOf(
            KEY_DOWNLOAD_TITLE to "Download Worker",
            KEY_DOWNLOAD_DESC to "Downlading Image is successfully"
        )

        val uploadData = workDataOf(
            KEY_UPLOAD_TITLE to "Upload Worker",
            KEY_UPLOAD_DESC to "Uploading Image is successfully"
        )

        val constraintUpload = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresCharging(true)
            .build()

        val constraintDownload = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresStorageNotLow(true)
            .setRequiresBatteryNotLow(true)
            .build()

        val compressImage = OneTimeWorkRequestBuilder<CompressWorker>()
            .build()

        val filterImage = OneTimeWorkRequestBuilder<FilterWorker>()
            .build()

        val uploadImage = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(constraintUpload)
            .setInputData(uploadData)
            .build()

        val downloadImage = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraintDownload)
            .setInputData(downloadData)
            .setInitialDelay(3, TimeUnit.SECONDS)
            .build()

        val parallelWork = mutableListOf<OneTimeWorkRequest>()
        parallelWork.add(compressImage)
        parallelWork.add(filterImage)

        workManager
            .beginWith(parallelWork)
            .then(uploadImage)
            .then(downloadImage)
            .enqueue()

        workManager.getWorkInfoByIdLiveData(downloadImage.id)
            .observe(this, Observer { workInfoDownload ->
                binding.textViewWorkState.text = workInfoDownload.state.name
            })
    }

    private fun setPeriodicallySendingLogs() {
        val workManager = WorkManager.getInstance(this)

        val sendingLog = PeriodicWorkRequestBuilder<SendLogWorker>(15, TimeUnit.MINUTES)
            .addTag(TAG_SEND_LOG)
            .build()

        workManager.enqueue(sendingLog)

        workManager.getWorkInfoByIdLiveData(sendingLog.id)
            .observe(this, Observer { workInfo ->
                binding.textViewWorkState.text = workInfo.state.name
            })
    }

    private fun cancellSendingLogRequest() {
        WorkManager.getInstance(this).cancelAllWorkByTag(TAG_SEND_LOG)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}