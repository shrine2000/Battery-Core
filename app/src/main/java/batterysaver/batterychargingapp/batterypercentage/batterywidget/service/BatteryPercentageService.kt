package batterysaver.batterychargingapp.batterypercentage.batterywidget.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.IBinder
import android.widget.RemoteViews
import java.util.*
import android.app.*
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import batterysaver.batterychargingapp.batterypercentage.batterywidget.MainActivity

import batterysaver.batterychargingapp.batterypercentage.batterywidget.R
import batterysaver.batterychargingapp.batterypercentage.batterywidget.preference.AppPreferences
import batterysaver.batterychargingapp.batterypercentage.batterywidget.util.ImageUtils
import kotlin.math.roundToInt

class BatteryPercentageService : Service() {


    private val notificationLayout by lazy {
        RemoteViews(
            "batterysaver.batterychargingapp.batterypercentage.batterywidget",
            R.layout.custom_notification_view
        )
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    private val builder by lazy {
        Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(Icon.createWithBitmap(ImageUtils.createBitmapFromString("0")))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(false)
            .setAutoCancel(true)
            .setCustomContentView(notificationLayout)
            .setContentIntent(createPendingIntent())
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
        if (AppPreferences.isBatteryPercentageNotiEnabled) {
            createNotificationChannel(CHANNEL_ID, "Battery Percentage Status Service")

            startForeground(NOTIFICATION_ID, builder.build())

            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryStatus: Intent? =
                IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                    this.registerReceiver(null, ifilter)
                }

            var batteryPercentage: Int
            var batteryTemperature: Int
            var batteryCurrent: Int

            Timer().scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    batteryPercentage =
                        batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    batteryTemperature =
                        batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) as Int
                    batteryCurrent =
                        batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

                    batteryTemperature /= 10
                    batteryCurrent = (batteryCurrent.toFloat() / 1000).roundToInt()

                    updateNotification(
                        batteryPercentage.toString().trim(),
                        batteryTemperature.toString().trim(),
                        batteryCurrent.toString().trim()
                    )
                }
            }, 0, 5000)
        } else notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun updateNotification(
        batteryPercentage: String,
        batteryTemperature: String,
        batteryCurrent: String
    ) {
        if (AppPreferences.isBatteryPercentageNotiEnabled) {
            val bitmap = ImageUtils.createBitmapFromString(batteryPercentage)
            val icon = Icon.createWithBitmap(bitmap)

            builder.setSmallIcon(icon)

            notificationLayout.setTextViewText(
                R.id.battery_percentage,
                "Now: ${batteryPercentage}% · ${batteryCurrent}mA · ${batteryTemperature}°C"
            )

            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }


    private fun createNotificationChannel(channelId: String, channelName: String): String? {

        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_LOW
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(chan)
        return  channelId
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }


    private fun createPendingIntent(): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    companion object {
        val NOTIFICATION_ID = 1
        val CHANNEL_ID = "battery_service"
    }
}