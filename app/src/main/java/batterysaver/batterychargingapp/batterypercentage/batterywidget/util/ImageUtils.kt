package batterysaver.batterychargingapp.batterypercentage.batterywidget.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

class ImageUtils {
    companion object {
        fun createBitmapFromString(batteryPercentage: String): Bitmap {
            val paint = Paint().apply {
                isAntiAlias = true
                textSize = 100F
                textAlign = Paint.Align.CENTER
            }

            val batteryBounds = Rect()
            paint.getTextBounds(batteryPercentage, 0, batteryPercentage.length, batteryBounds)
            val width = batteryBounds.width()
            val bitmap = Bitmap.createBitmap(width + 50, 110, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawText(batteryPercentage, (width.toFloat()), 80F, paint)
            return bitmap
        }
    }
}