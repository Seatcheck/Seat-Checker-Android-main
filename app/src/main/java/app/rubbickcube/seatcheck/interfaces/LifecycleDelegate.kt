package app.rubbickcube.seatcheck

interface LifecycleDelegate {
    fun onAppBackgrounded()
    fun onAppForegrounded()
}