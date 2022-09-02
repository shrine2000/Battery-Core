package batterysaver.batterychargingapp.batterypercentage.batterywidget

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import batterysaver.batterychargingapp.batterypercentage.batterywidget.utils.APP_PRIVACY_POLICY_LINK
import batterysaver.batterychargingapp.batterypercentage.batterywidget.utils.GMAIL_PACKAGE_ID
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class Settings : AppCompatActivity() {

    lateinit var version_tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.toolbar))
        version_tv = findViewById(R.id.version_tv)
        version_tv.apply {
            text = packageManager.getPackageInfo(packageName, 0).versionName
        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setSupportActionBar(findViewById(R.id.toolbar_settings))
        supportActionBar?.elevation = 4F

        findViewById<Toolbar>(R.id.toolbar_settings).apply {
            setBackgroundColor(android.graphics.Color.WHITE)
            navigationIcon?.mutate()?.let {
                it.setTint(android.graphics.Color.BLACK)
                navigationIcon = it
            }

            setNavigationOnClickListener {
                this@Settings.finish()
            }
            title = "Settings"
            setTitleTextColor(android.graphics.Color.BLACK)
        }

    }

    private fun openSourceLicense() {
        startActivity(Intent(this@Settings, OssLicensesMenuActivity::class.java))
        OssLicensesMenuActivity.setActivityTitle(" Notices ")
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.feedback -> {
                val listFeedback = listOf(
                    " Suggestion for new feature",
                    " Report a bug ",
                    " I like this app \uD83D\uDC4D",
                    " Other"
                )

                simpleListDialog(" Select your message", listFeedback) { t ->
                    val m = if (t == "Other") " Please enter your feedback" else t
                    sendFeedback(m)
                }
            }
            R.id.share -> {
                try {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Battery Core")
                    var shareMessage = "\n Download this great app to monitor battery health, with excellent UI & lot of details like battery temperature, voltage etc\n\n"
                    shareMessage = "$shareMessage Google play store - https://play.google.com/store/apps/details?id=batterysaver.batterychargingapp.batterypercentage.batterywidget"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                    startActivity(Intent.createChooser(shareIntent, "Choose one"))
                } catch (e: java.lang.Exception) {

                }
            }
            R.id.license -> {
                openSourceLicense()
            }
            R.id.bug -> {
                sendFeedback("Bug Report")
            }

            R.id.privacy -> {
                openUrl(APP_PRIVACY_POLICY_LINK)
            }

        }
    }


    private fun simpleListDialog(
        title: String = "",
        list: List<String>,
        onSelected: (String) -> Unit = {}
    ) {
        var selectedString: String

        MaterialDialog(this).show {
            debugMode(false)
            cornerRadius(16f)
            title(text = title)
            listItems(items = list) { _, _, text ->
                selectedString = text.toString()
                onSelected.invoke(selectedString)
            }
        }
    }


    private fun sendFeedback(message: String = "") {
        val isGmail = GMAIL_PACKAGE_ID.isAppInstalled()
        var body = ""
        try {
            body = packageManager.getPackageInfo(packageName, 0).versionName
            body =
                message + "\n\n-----------------------------\n Following details are collected to find the bugs/issues." +
                        " If you are not interested to share this you may delete it. \n" +
                        " \n Device OS version: " +
                        Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                        "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER + "\n\n"
        } catch (e: Exception) {

        }

        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
                putExtra(Intent.EXTRA_SUBJECT, "Battery Core - Query")
                putExtra(Intent.EXTRA_TEXT, body)
            }

            if (isGmail) {
                intent.setPackage(GMAIL_PACKAGE_ID)
            }

            this.startActivity(Intent.createChooser(intent, "Select Email App"))

        } catch (e: Exception) {

        }
    }

    private fun String.isAppInstalled(): Boolean {
        val pm = packageManager!!
        try {
            pm.getPackageInfo(this, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {

        }
        return false
    }

    private fun openUrl(string: String = "") {

        val uri: Uri = Uri.parse(string)

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags =
            Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK

        try {
            startActivity(intent)
        } catch (e: java.lang.RuntimeException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(string)
                )
            )
        }
    }
}