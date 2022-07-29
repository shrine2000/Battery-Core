package batterysaver.batterychargingapp.batterypercentage.batterywidget

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import batterysaver.batterychargingapp.batterypercentage.batterywidget.preference.AppPreferences
import batterysaver.batterychargingapp.batterypercentage.batterywidget.preference.AppRater
import batterysaver.batterychargingapp.batterypercentage.batterywidget.service.BatteryPercentageService
import batterysaver.batterychargingapp.batterypercentage.batterywidget.utils.vibrateDevice
import com.db.williamchart.view.LineChartView
import com.github.pwittchen.rxbattery.library.RxBattery
import com.google.firebase.analytics.FirebaseAnalytics
import eo.view.batterymeter.BatteryMeterView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import java.lang.Exception
import java.util.*
import kotlin.math.roundToInt


/*
*
* TODO
*  1. https://stackoverflow.com/questions/19932442/android-runningtaskinfo
*
*  */

class MainActivity : AppCompatActivity() {

    lateinit var batteryMeterView: BatteryMeterView
    lateinit var lineChartView: LineChartView
    lateinit var min_tv: TextView
    lateinit var present_tv: TextView
    lateinit var max_tv: TextView
    lateinit var present_charge_tv: TextView
    lateinit var battery_capacity_tv: TextView
    lateinit var charged_today_tv: TextView
    lateinit var health_tv: TextView
    lateinit var power_tv: TextView
    lateinit var temperature_tv: TextView
    lateinit var voltage_tv: TextView
    lateinit var level_tv: TextView
    lateinit var charge_type_tv: TextView
    lateinit var battery_usage_tv: TextView
    lateinit var settings_tv: TextView

    lateinit var charge_percentage_ll: LinearLayout
    lateinit var timer: Timer
    lateinit var batteryManager: BatteryManager
    lateinit var show_battery_percentage: SwitchCompat
    lateinit var intentService: Intent

    lateinit var batteryStatsReceiver: BroadcastReceiver

    val currentArray = arrayListOf<Pair<String, Float>>()
    val tempCurrentArray = arrayListOf<Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intentService = Intent(this@MainActivity, BatteryPercentageService::class.java)


        initLayout()
        AppPreferences.init(this)
        setBatteryView()
        findCharging()
        createNotificationChannel()
        setLineChartCharging()
        initNotification()
        AppRater.appLaunched(this)
        FirebaseAnalytics.getInstance(this)


        // https://stackoverflow.com/a/51265574/9846650
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.elevation = 4F

        timer = Timer()

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        // registerReceiver(batteryStatsReceiver, filter)
        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        timer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val batteryCurrent =
                        batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    updateCurrentChart(batteryCurrent)
                }
            }
        }, 0, 1000)

        setSupportActionBar(
            findViewById(R.id.toolbar)
        )

    }

    private fun initNotification() {
        show_battery_percentage.setOnCheckedChangeListener { buttonView, isChecked ->
            AppPreferences.isBatteryPercentageNotiEnabled = isChecked
            if (isChecked) {
                intentService.setPackage(this.packageName)
                startService(intentService)
            } else {
                intentService.setPackage(this.packageName)
                stopService(intentService)
            }
        }

        show_battery_percentage.isChecked = AppPreferences.isBatteryPercentageNotiEnabled

        if (AppPreferences.isBatteryPercentageNotiEnabled) {
            intentService.setPackage(this.packageName)
            startService(intentService)
        } else {
            intentService.setPackage(this.packageName)
            stopService(intentService)
        }
    }

    private fun fillCurrentArray() {
        repeat(13) {
            currentArray.add(Pair(" ", 0f))
        }
    }

    private fun initLayout() {
        batteryMeterView = findViewById(R.id.battery_meter_view)
        lineChartView = findViewById(R.id.line_chart_view)
        show_battery_percentage = findViewById(R.id.notification_switch)
        battery_usage_tv = findViewById(R.id.battery_usage_tv)
        min_tv = findViewById(R.id.min_current)
        present_tv = findViewById(R.id.present_current)
        max_tv = findViewById(R.id.max_current)
        present_charge_tv = findViewById(R.id.present_charge)
        battery_capacity_tv = findViewById(R.id.battery_capacity_tv)
        // charged_today_tv = findViewById(R.id.charged_today_tv)
        health_tv = findViewById(R.id.health_tv)
        power_tv = findViewById(R.id.power_tv)
        temperature_tv = findViewById(R.id.temperature_tv)
        voltage_tv = findViewById(R.id.voltage_tv)
        level_tv = findViewById(R.id.level_tv)
        settings_tv = findViewById(R.id.settings_tv)
        charge_type_tv = findViewById(R.id.charge_type_tv)
        charge_percentage_ll = findViewById(R.id.charge_percentage_ll)

        val scrollView = findViewById<View>(R.id.scroll_view) as ScrollView
        OverScrollDecoratorHelper.setUpOverScroll(scrollView)

        charge_percentage_ll.setOnClickListener {
            vibrateDevice(this@MainActivity)
        }
        battery_usage_tv.setOnClickListener {
            openPowerUsageStatistics()
        }
        settings_tv.setOnClickListener {
            startActivity(Intent(this@MainActivity, Settings::class.java))
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Battery Percentage"
            val description = "Battery Percentage"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("default", name, importance)
            channel.description = description
            channel.setSound(null, null)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setPowerTextView() {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            this.registerReceiver(null, filter)
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        power_tv.text = getStatusString(status)
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    fun setBatteryView() {
        RxBattery.observe(this)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                present_charge_tv.text = it.level.toString() + "%"
                health_tv.text = getHealthString(it.healthCode)
                temperature_tv.text = (it.temperature / 10).toString() + " °C"
                voltage_tv.text = it.voltage.toString() + " mV"

                level_tv.text = it.level.toString() + "%"
                charge_type_tv.text = getPlugTypeString(it.pluggedCode)

                batteryMeterView.apply {
                    chargeLevel = it.level
                    isCharging = it.pluggedCode != 0
                }
                setPowerTextView()
            }
    }


    private fun getHealthString(health: Int): String {
        var healthString = "Unknown"
        when (health) {
            BatteryManager.BATTERY_HEALTH_DEAD -> healthString = "Dead"
            BatteryManager.BATTERY_HEALTH_GOOD -> healthString = "Good"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> healthString = "Over Voltage"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> healthString = "Over Heat"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> healthString = "Failure"
        }
        return healthString
    }

    private fun getStatusString(status: Int): String {
        var statusString = "Unknown"
        when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> statusString = "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> statusString = "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> statusString = "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> statusString = "Not Charging"
        }
        return statusString
    }

    private fun getPlugTypeString(plugged: Int): String {
        var plugType = "Unknown"
        when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> plugType = "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> plugType = "USB"
        }
        return plugType
    }

    fun updateVoltageChart(voltage: Int) {

    }


    fun updateCurrentChart(current: Int) {

        if (::lineChartView.isInitialized) {
            val v = (current.toFloat() / 1000).roundToInt().toFloat()
            tempCurrentArray.add(v)

            min_tv.text = tempCurrentArray.toList().minOrNull().toString() + " mA"
            present_tv.text = "$v mA"
            max_tv.text = tempCurrentArray.toList().maxOrNull().toString() + " mA"

            currentArray.add(Pair("", v))

            if (currentArray.size >= 30) {
                currentArray.removeAt(0)
            }
            lineChartView.animate(currentArray.toList())
        }
    }


    fun setLineChartDisCharging() {
        fillCurrentArray()

        if (::lineChartView.isInitialized) {
            lineChartView.apply {
                lineColor = Color.parseColor("#0383da")
                gradientFillColors =
                    intArrayOf(
                        Color.parseColor("#0392f3"),
                        Color.TRANSPARENT
                    )
                animation.duration = 0L
                smooth = true
                lineThickness = 3F
            }
        }
    }


    private fun findCharging() {
        /* batteryStatsReceiver = object : BroadcastReceiver() {
             @SuppressLint("SetTextI18n")
             override fun onReceive(context: Context, intent: Intent) {

                 if (Build.VERSION.SDK_INT >= 28) {
                     val mBatteryManager =
                         context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                     val remainingTimeToCharge = mBatteryManager.computeChargeTimeRemaining()
                 }


                 val isPresent = intent.getBooleanExtra("present", false)
                 val technology = intent.getStringExtra("technology")
                 val plugged = intent.getIntExtra("plugged", -1)
                 val scale = intent.getIntExtra("scale", -1)
                 val health = intent.getIntExtra("health", 0)
                 // val status = intent.getIntExtra("status", 0)
                 val rawLevel = intent.getIntExtra("level", -1)
                 val voltage = intent.getIntExtra("voltage", 0)
                 val fastchargestatus = intent.getBooleanExtra("fastcharge_status", false)
                 val temperature = intent.getIntExtra("temperature", 0)
                 var level = 0


                 val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                 val batteryStatus = context.registerReceiver(null, ifilter)


                 val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                 val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                         status == BatteryManager.BATTERY_STATUS_FULL


                 if (isPresent) {


                 }
             }
         }*/

    }

    private fun setLineChartCharging() {
        fillCurrentArray()

        if (::lineChartView.isInitialized) {
            lineChartView.apply {
                lineColor = Color.parseColor("#03DAC6")
                gradientFillColors =
                    intArrayOf(
                        Color.parseColor("#74f2ce"),
                        Color.TRANSPARENT
                    )
                animation.duration = 0L
                smooth = true
                lineThickness = 3F
            }
        }
    }

    private fun openPowerUsageStatistics() {

        try {
            startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY))

        } catch (e: Exception) {

        }

        /*
            *  if (MANUFACTURER == "samsung") {
                 val intent = Intent()
                 if (VERSION.SDK_INT > VERSION_CODES.N) {
                     intent.component = ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
                 } else if (VERSION.SDK_INT > VERSION_CODES.LOLLIPOP) {
                     intent.component = ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity")
                 }

                 try {
                    startActivity(intent)
                 } catch (ex: ActivityNotFoundException) {
                     startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY))
                 }
             } else {
                 startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY))
             }
            */
    }

    override fun onPause() {
        super.onPause()
        /*if (::timer.isInitialized){
            timer.cancel()
        }*/
        //  unregisterReceiver(batteryStatsReceiver)
    }


     override fun onResume() {
        super.onResume()
        /*timer = Timer()
        if (::timer.isInitialized && ::batteryManager.isInitialized){
            timer.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        val batteryCurrent =
                            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                        updateCurrentChart(batteryCurrent)
                    }
                }
            }, 0, 1000)
        }*/
        // val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        // registerReceiver(batteryStatsReceiver, filter)
    }

}