package com.mikori.kids.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.mikori.kids.data.remote.dto.UsageItem
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recolecta el tiempo de uso por aplicación del día actual usando la API oficial
 * UsageStatsManager. Solo lee tiempos de uso; no accede a contenido (mecanismo
 * legítimo de control parental).
 */
@Singleton
class UsageStatsCollector @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun collectToday(): List<UsageItem> {
        if (!UsageAccess.isGranted(context)) return emptyList()

        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val start = startOfToday()
        val now = System.currentTimeMillis()
        val date = todayString()

        val stats = usm.queryAndAggregateUsageStats(start, now) // Map<packageName, UsageStats>
        val pm = context.packageManager
        val self = context.packageName

        return stats.values
            .asSequence()
            .filter { it.packageName != self && it.totalTimeInForeground > 0 }
            .map { s ->
                UsageItem(
                    `package` = s.packageName,
                    label = resolveLabel(pm, s.packageName),
                    category = null,
                    date = date,
                    seconds = (s.totalTimeInForeground / 1000L).toInt(),
                )
            }
            .filter { it.seconds > 0 }
            .sortedByDescending { it.seconds }
            .toList()
    }

    private fun resolveLabel(pm: PackageManager, pkg: String): String? = try {
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (_: Exception) {
        null
    }

    private fun startOfToday(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun todayString(): String {
        val cal = Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
        )
    }
}
