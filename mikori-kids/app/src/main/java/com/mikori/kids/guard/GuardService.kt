package com.mikori.kids.guard

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/**
 * Arranque/parada del servicio de vigilancia.
 */
object GuardService {
    fun start(context: Context) {
        val intent = Intent(context, MikoriGuardService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
        context.stopService(Intent(context, MikoriGuardService::class.java))
    }
}
