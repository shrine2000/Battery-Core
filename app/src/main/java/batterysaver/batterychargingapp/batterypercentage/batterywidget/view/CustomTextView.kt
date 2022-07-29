package batterysaver.batterychargingapp.batterypercentage.batterywidget.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity


class CustomTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr){
    init {
        this.apply {
            val myTypeface = Typeface.createFromAsset(
                context.assets,
                "font/Roboto-Regular.ttf"
            )
            typeface = myTypeface
            gravity = Gravity.CENTER
        }
    }
}