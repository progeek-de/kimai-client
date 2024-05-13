package de.progeek.kimai.android

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import de.progeek.kimai.shared.R
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import de.progeek.kimai.shared.kimaiDispatchers
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StopBroadcastHelper : KoinComponent {
    private val timesheetRepository by inject<TimesheetRepository>()
    private val scope = CoroutineScope(kimaiDispatchers.io)

    fun stopTimesheet() {
        scope.launch {
            timesheetRepository.getActiveTimesheet().onSuccess {
                if (it.id != null && it.id?.toInt() != -1) {
                    timesheetRepository.stopTimesheet(it.id!!)
                }
            }
        }
    }
}

class StopReceiver : BroadcastReceiver() {
    private val helper by lazy { StopBroadcastHelper() }

    override fun onReceive(context: Context?, intent: Intent?) {
        helper.stopTimesheet()
    }
}

class NotificationService : Service() {
    private val channelId = "kimai_channel"
    private val notificationId = 70

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            channelId,
            StringDesc.Resource(StringResource(R.string.appName)).toString(this),
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification() = NotificationCompat.Builder(this, channelId)
        .setContentTitle(StringDesc.Resource(StringResource(R.string.appName)).toString(this))
        .setContentText(StringDesc.Resource(StringResource(R.string.notification_text)).toString(this))
        .setSmallIcon(R.drawable.progeek_icon_clear)
        .setOngoing(true)
        .addAction(
            R.drawable.progeek_icon_clear,
            StringDesc.Resource(StringResource(R.string.stop)).toString(this),
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(this, StopReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .setColor(ColorResource(R.color.notification_color).getColor(this))
        .build()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(notificationId, buildNotification())

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }
}


