package app.rubbickcube.sido.fonts

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.widget.TextView

/**
 * Created by hp on 5/16/2016.
 */
class robotoThinItalic : TextView {
    constructor(context: Context) : super(context) {
        setFont()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setFont()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setFont()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun setFont() {
        val font = Typeface.createFromAsset(context.assets, "fonts/Roboto-Thin.ttf")
        setTypeface(font, Typeface.BOLD_ITALIC)
    }


    fun updateTextSize(context: Context) {
        typeface
        val currentTextSize = textSize
        val otherSettings = context.getSharedPreferences("settings", 0)
        val newScale = otherSettings.getFloat("key_scaling", 1f)
        textSize = newScale * currentTextSize
    }
}
