package com.mikori.kids.guard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Permiso "Mostrar sobre otras apps" (SYSTEM_ALERT_WINDOW), necesario para dibujar
 * la pantalla de descanso. Se concede en Ajustes.
 */
object OverlayPermission {
    fun isGranted(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun settingsIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
