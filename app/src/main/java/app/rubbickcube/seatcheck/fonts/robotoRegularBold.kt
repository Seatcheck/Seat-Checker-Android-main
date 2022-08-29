package app.rubbickcube.sido.fonts


import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView

class robotoRegularBold : TextView {
    constructor(context: Context) : super(context) {
        setFont()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setFont()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setFont()
    }

    private fun setFont() {
        val font = Typeface.createFromAsset(context.assets, "fonts/Roboto-Regular.ttf")
        setTypeface(font, Typeface.BOLD)
        //		setTextSize(EndPointsConstants.fontsize);
        //		setTextSize(40);
        //		setLineSpacing(5,2.5f);
        //		setTextColor(getResources().getColor(R.color.priwhite));
    }


    fun updateTextSize(context: Context) {
        typeface
        val currentTextSize = textSize
        val otherSettings = context.getSharedPreferences("settings", 0)
        val newScale = otherSettings.getFloat("key_scaling", 1f)
        textSize = newScale * currentTextSize
    }
}