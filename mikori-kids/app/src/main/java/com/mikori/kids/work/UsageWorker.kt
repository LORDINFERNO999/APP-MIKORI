package com.mikori.kids.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mikori.kids.data.repository.UsageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Recolecta y sube el uso del día, y envía un latido. Programado por WorkManager
 * de forma periódica, respetando las restricciones de batería de Android.
 */
@HiltWorker
class UsageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val usageRepository: UsageRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            usageRepository.heartbeat()
            val ok = usageRepository.collectAndUpload()
            if (ok) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
