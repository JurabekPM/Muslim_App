package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation

internal class CounterTapCadence {
    private var lastTapMillis: Long? = null

    fun recordTap(nowMillis: Long): Int {
        val previousTap = lastTapMillis
        lastTapMillis = nowMillis
        if (previousTap == null) return SlowDurationMillis

        // Leave a little room for the digit to settle before the next tap.
        val interval = (nowMillis - previousTap).coerceIn(0L, 575L)
        return (interval * 4 / 5).toInt().coerceIn(80, SlowDurationMillis)
    }

    fun reset() {
        lastTapMillis = null
    }

    companion object {
        const val SlowDurationMillis = 460
    }
}
