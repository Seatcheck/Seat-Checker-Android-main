package app.rubbickcube.seatcheck.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior


class CustomBottomSheetBehaviour<V : View?> : BottomSheetBehavior<View> {
    constructor() : super() {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}

    fun onInterceptTouchEvent(parent: CoordinatorLayout, child: Any, event: MotionEvent): Boolean {
        return false
    }
}