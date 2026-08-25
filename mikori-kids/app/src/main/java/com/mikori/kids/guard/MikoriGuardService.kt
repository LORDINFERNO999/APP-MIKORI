package com.mikori.kids.guard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.mikori.kids.R
import com.mikori.kids.data.remote.dto.PolicyData
import com.mikori.kids.data.repository.PolicyRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Servicio en primer plano que aplica la política de control (V2):
 * detecta la app en primer plano y muestra la pantalla de descanso (overlay)
 * cuando corresponde. Ver docs/04-v2-control.md.
 */
@AndroidEntryPoint
class MikoriGuardService : Service() {

    @Inject
    lateinit var policyRepository: PolicyRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startAsForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch { monitorLoop() }
        return START_STICKY
    }

    private suspend fun monitorLoop() {
        policyRepository.loadFromCache()
        var tick = 0
        while (scope.isActive) {
            // Refresca la política del servidor cada ~30 s (cada 15 ciclos de 2 s).
            if (tick % 15 == 0) {
                runCatching { policyRepository.refresh() }
            }
            val policy = policyRepository.current()
            val pkg = ForegroundApp.current(this)
            val block = shouldBlock(policy, pkg)

            withContext(Dispatchers.Main) {
                if (block) showOverlay() else hideOverlay()
            }

            tick++
            delay(2_000)
        }
    }

    private fun shouldBlock(policy: PolicyData?, pkg: String?): Boolean {
        if (policy == null || pkg == null) return false
        if (pkg == packageName) return false // nunca bloquear la propia MIKORI Kids
        if (policy.blockAll) return true
        if (policy.blockedPackages.contains(pkg)) return true
        if (policy.appLimits.any { it.`package` == pkg && it.exceeded }) return true
        return false
    }

    // ── Overlay ──────────────────────────────────────────────────────────
    private fun showOverlay() {
        if (overlayView != null) return
        val view = buildOverlayView()
        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.OPAQUE,
        )
        runCatching {
            windowManager?.addView(view, params)
            overlayView = view
        }
    }

    private fun hideOverlay() {
        overlayView?.let { v ->
            runCatching { windowManager?.removeView(v) }
        }
        overlayView = null
    }

    /** Pantalla de descanso, tono nocturno, lenguaje amable (nunca "bloqueado"). */
    private fun buildOverlayView(): View {
        val night = Color.parseColor("#14141A")
        val matcha = Color.parseColor("#A6CBA0")
        val onSurface = Color.parseColor("#E7E3DA")

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(night)
            setPadding(64, 64, 64, 64)
        }
        container.addView(TextView(this).apply {
            text = "🌙"
            textSize = 48f
            gravity = Gravity.CENTER
        })
        container.addView(TextView(this).apply {
            text = "¡Hora de descansar!"
            textSize = 26f
            setTextColor(matcha)
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 16)
        })
        container.addView(TextView(this).apply {
            text = "Ya usaste tu tiempo de pantalla por ahora. Podrás volver a jugar y descubrir más tarde. 🌱"
            textSize = 16f
            setTextColor(onSurface)
            gravity = Gravity.CENTER
        })
        container.addView(TextView(this).apply {
            text = "mikori"
            textSize = 18f
            setTextColor(matcha)
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 0)
        })

        return FrameLayout(this).apply {
            setBackgroundColor(night)
            addView(container)
        }
    }

    // ── Foreground / notificación ────────────────────────────────────────
    private fun startAsForeground() {
        val channelId = "mikori_guard"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "MIKORI",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "MIKORI está cuidando este dispositivo" }
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("MIKORI está activo")
            .setContentText("Cuidando este dispositivo junto a tu familia")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIF_ID, notification, type)
    }

    override fun onDestroy() {
        hideOverlay()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID = 4201
    }
}
