package com.example.htmlbackgroundrunner

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder

class RunnerService : Service() {
    companion object {
        const val ACTION_START = "trading.lab.START"
        const val ACTION_STOP = "trading.lab.STOP"
        const val CHANNEL_ID = "trading_lab_status"
        const val NOTIFICATION_ID = 41
        @Volatile private var active = false
        fun start(context: Context) {
            val intent = Intent(context, RunnerService::class.java).setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent) else context.startService(intent)
        }
        fun stop(context: Context) {
            context.startService(Intent(context, RunnerService::class.java).setAction(ACTION_STOP))
        }
        fun isRunning(context: Context): Boolean = active
    }

    override fun onCreate() { super.onCreate(); createChannel() }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                active = false
                getSharedPreferences("trading_lab", MODE_PRIVATE).edit().putBoolean("running", false).apply()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                active = true
                getSharedPreferences("trading_lab", MODE_PRIVATE).edit().putBoolean("running", true).apply()
                startForeground(NOTIFICATION_ID, notification())
                return START_STICKY
            }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL_ID, "Trading research engine", NotificationManager.IMPORTANCE_LOW)
            channel.description = "Visible status for a user-started research session"
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun notification(): Notification {
        val open = PendingIntent.getActivity(this, 1, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 2, Intent(this, RunnerService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, CHANNEL_ID) else Notification.Builder(this)
        return builder.setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Trading Strategy Lab")
            .setContentText("Research session is active")
            .setContentIntent(open).setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Open", open).build())
            .addAction(Notification.Action.Builder(null, "Stop", stop).build()).build()
    }

    override fun onDestroy() {
        active = false
        getSharedPreferences("trading_lab", MODE_PRIVATE).edit().putBoolean("running", false).apply()
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
