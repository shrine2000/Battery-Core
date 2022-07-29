package batterysaver.batterychargingapp.batterypercentage.batterywidget.preference


import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val NAME = "battery_core"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

     private val BATTERY_PERCENTAGE_NOTI = Pair("isDark", true)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }



    var isBatteryPercentageNotiEnabled: Boolean
        get() = preferences.getBoolean(BATTERY_PERCENTAGE_NOTI.first, BATTERY_PERCENTAGE_NOTI.second)
        set(value) = preferences.edit {
            it.putBoolean(BATTERY_PERCENTAGE_NOTI.first, value)
        }
}