package com.example.workmanagerexample

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.workmanagerexample.MainActivity.Companion.KEY_USER_COMMENT_TEXT

class EmotionAnalysisWorker(context: Context, userParameters: WorkerParameters) :
    Worker(context, userParameters) {

    companion object {
        val KEY_USER_EMOTION_RESULT = "key.user.emotion.result"
    }

    override fun doWork(): Result {
        val text = inputData.getString(KEY_USER_COMMENT_TEXT)

        try {
            for (i in 0..200) {
                Log.i("furkanpasa", "Emotion Analysing $i")
            }

            val outputData = Data.Builder()
                .putString(KEY_USER_EMOTION_RESULT, getUserEmotion(text))
                .build()

            return Result.success(outputData)
        } catch (e: Exception) {
            return Result.failure()
        }

    }

    fun getUserEmotion(userText : String?): String {

        val emotionList = listOf("Sad", "Happy", "Angry", "Surprise", "Tired", "Bored")

        return emotionList.random()
    }

}