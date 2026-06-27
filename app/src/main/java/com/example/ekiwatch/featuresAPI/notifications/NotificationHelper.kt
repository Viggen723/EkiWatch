package com.example.ekiwatch.featuresAPI.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ekiwatch.R

/**
 * Creates the notification channel and posts the "arriving soon" alert when a
 * station geofence is entered. Pulled out of GeofenceBroadcastReceiver so the
 * receiver only has to decide *what* to say, not how Android plumbing works.
 */
object NotificationHelper {

    const val CHANNEL_ID = "station_arrival_channel"
    private const val CHANNEL_NAME = "Station Arrivals"
    private const val CHANNEL_DESCRIPTION =
        "Alerts you when you're arriving at a station with nearby landmarks"

    // Make a channel to be able to pass the notification. See the androidManifest too
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    /**
     * Posts the arrival notification. stationId's hashCode is used as the
     * notification id so repeat triggers for the *same* station update one
     * notification instead of stacking duplicates, while different stations
     * still get their own.
     */
    fun showStationArrivalNotification(
        context: Context,
        stationId: String,
        title: String,
        body: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_train_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // POST_NOTIFICATIONS is requested in MapUIComponent before geofences
        // are ever registered, but we guard here too in case that flow changes.
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(stationId.hashCode(), notification)
        }
    }
}