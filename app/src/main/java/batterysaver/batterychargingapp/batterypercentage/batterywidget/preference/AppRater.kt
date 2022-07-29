package batterysaver.batterychargingapp.batterypercentage.batterywidget.preference

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import batterysaver.batterychargingapp.batterypercentage.batterywidget.R
import com.afollestad.materialdialogs.MaterialDialog

object AppRater {
    private const val DO_NOT_SHOW_AGAIN = "do_not_show_again"// Package Name
    private const val APP_RATING = "app_rating"// Package Name
    private const val LAUNCH_COUNT = "launch_count"// Package Name
    private const val DATE_FIRST_LAUNCH = "date_first_launch"// Package Name

    private const val DAYS_UNTIL_PROMPT = 1 //Min number of days
    private const val LAUNCHES_UNTIL_PROMPT = 3 //Min number of launches

    @JvmStatic
    fun appLaunched(context: Activity) {
        val prefs = context.getSharedPreferences(APP_RATING, 0)
        if (prefs.getBoolean(DO_NOT_SHOW_AGAIN, false)) {
            return
        }

        val editor = prefs.edit()

        // Increment launch counter
        val launchCount = prefs.getLong(LAUNCH_COUNT, 0) + 1
        editor.putLong(LAUNCH_COUNT, launchCount)

        // Get date of first launch
        var dateFirstLaunch = prefs.getLong(DATE_FIRST_LAUNCH, 0)
        if (dateFirstLaunch == 0L) {
            dateFirstLaunch = System.currentTimeMillis()
            editor.putLong(DATE_FIRST_LAUNCH, dateFirstLaunch)
        }

        // Wait at least n days before opening
        if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= dateFirstLaunch + DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000) {
                showRateDialog(context, editor)
            }
        }

        editor.apply()
    }

    private fun showRateDialog(context: Context, editor: SharedPreferences.Editor) {
        MaterialDialog(context).show {
            cornerRadius(16f)
            setTitle("Rate this App")
            message(text = "If you enjoy using this app, please take a moment to rate it. Thanks for your support!")
            positiveButton(R.string.app_name) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${context.packageName}")
                    )
                )
                editor.apply {
                    putBoolean(DO_NOT_SHOW_AGAIN, true)
                    commit()
                }
            }
            neutralButton(text = "Not now")
            negativeButton(text = "No thanks") {
                editor.putBoolean(DO_NOT_SHOW_AGAIN, true)
                editor.commit()
            }
        }

    }
}