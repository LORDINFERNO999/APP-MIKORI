package com.mikori.kids.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reprograma la recolección de uso tras reiniciar el dispositivo.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            WorkScheduler.schedulePeriodic(context)
        }
    }
}
