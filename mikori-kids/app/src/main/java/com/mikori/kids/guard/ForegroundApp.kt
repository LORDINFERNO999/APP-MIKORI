package com.mikori.kids.guard

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

/**
 * Detecta la app actualmente en primer plano mediante UsageStatsManager
 * (API oficial; no usa AccessibilityService).
 */
object ForegroundApp {
    fun current(context: Context): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val end = System.currentTimeMillis()
        val begin = end - 10_000L
        val events = usm.queryEvents(begin, end)
        val event = UsageEvents.Event()
        var pkg: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                pkg = event.packageName
            }
        }
        return pkg
    }
}
