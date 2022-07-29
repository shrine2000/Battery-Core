package batterysaver.batterychargingapp.batterypercentage.batterywidget.preference


import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val NAME = "battery_core"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    private val FILE_SAVE_COUNTER = Pair("launch_counter", 1)
    private val SAF_URI = Pair("saf_uri", "")
    private val BATTERY_PERCENTAGE_NOTI = Pair("isDark", true)
    private val DONT_SHOW_AGAIN = Pair("dont_show_again", false)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var dontShowAgain: Boolean
        get() = preferences.getBoolean(DONT_SHOW_AGAIN.first, DONT_SHOW_AGAIN.second)
        set(value) = preferences.edit {
            it.putBoolean(DONT_SHOW_AGAIN.first, value)
        }

    var fileSaveCounter: Int
        get() = preferences.getInt(FILE_SAVE_COUNTER.first, FILE_SAVE_COUNTER.second)
        set(value) = preferences.edit {
            it.putInt(FILE_SAVE_COUNTER.first, value)
        }

    var safUri: String
        get() = preferences.getString(SAF_URI.first, SAF_URI.second)!!
        set(value) = preferences.edit {
            it.putString(SAF_URI.first, value)
        }

    var isBatteryPercentageNotiEnabled: Boolean
        get() = preferences.getBoolean(BATTERY_PERCENTAGE_NOTI.first, BATTERY_PERCENTAGE_NOTI.second)
        set(value) = preferences.edit {
            it.putBoolean(BATTERY_PERCENTAGE_NOTI.first, value)
        }
}