package com.mikori.kids.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Programación de la recolección de uso. Periódica (batería-friendly) + puntual.
 */
object WorkScheduler {
    private const val PERIODIC = "mikori_usage_periodic"
    private const val ONE_TIME = "mikori_usage_once"

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Programa la recolección cada 15 minutos (mínimo permitido por WorkManager). */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<UsageWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    /** Fuerza una recolección inmediata (p. ej. tras vincular o al abrir la app). */
    fun runOnce(context: Context) {
        val request = OneTimeWorkRequestBuilder<UsageWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC)
    }
}
