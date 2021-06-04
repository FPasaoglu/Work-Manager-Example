package com.example.workmanagerexample

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class CompressWorker(context: Context, userParameters: WorkerParameters) : Worker(context, userParameters) {

    override fun doWork(): Result {
        try {
            for (i in 0..900) {
                Log.i("furkanpasa", "Compressing image $i")
            }
            return Result.success()
        }catch (e: Exception) {
            return Result.failure()
        }
    }
}