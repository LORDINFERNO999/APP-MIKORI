package com.mikori.kids.core.util

object TimeFormat {
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

    fun humanizeMinutes(minutes: Int): String = humanize(minutes * 60)
}
