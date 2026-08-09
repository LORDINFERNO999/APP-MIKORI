package com.mikori.parent.core.util

/**
 * Formateo de duraciones para la UI de MIKORI.
 */
object TimeFormat {

    /** 5040 -> "1h 24min" · 1140 -> "19min" · 0 -> "0min" */
    fun humanize(seconds: Int): String {
        if (seconds <= 0) return "0min"
        val totalMinutes = seconds / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
            hours > 0 -> "${hours}h"
            else -> "${minutes}min"
        }
    }

    /** Minutos -> "2h" / "2h 30min" (para límites). */
    fun humanizeMinutes(minutes: Int): String = humanize(minutes * 60)
}
