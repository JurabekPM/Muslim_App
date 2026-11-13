package uz.turgunboyevjurabek.muslimapp.feature.data.location

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Surface
import uz.turgunboyevjurabek.muslimapp.core.utils.CompassHeading
import uz.turgunboyevjurabek.muslimapp.core.utils.HeadingFilter
import uz.turgunboyevjurabek.muslimapp.core.utils.isMagneticFieldUsable
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt
import javax.inject.Inject

data class CompassReading(
    val heading: Float?,
    val rawHeading: Float? = null,
    val reliable: Boolean = false,
    val canConfirm: Boolean = false,
    val timestamp: Long = 0L,
)

class CompassSensorManager @Inject constructor(
    private val sensorManager: SensorManager,
) : SensorEventListener {
    private val acceleration = FloatArray(3)
    private val magneticField = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val displayMatrix = FloatArray(9)
    private val compassHeading = CompassHeading()
    private val filter = HeadingFilter()
    private val handler = Handler(Looper.getMainLooper())
    private var hasAcceleration = false
    private var magneticTimestamp = 0L
    private var magneticAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE
    private var usesRotationVector = false
    private var monitorsMagnetometer = false
    private var active = false
    private var lastReadingTime = 0L
    private var lastDisplayRotation = -1
    private var screenX = SensorManager.AXIS_X
    private var screenY = SensorManager.AXIS_Y
    private var displayRotation = { Surface.ROTATION_0 }
    private var expectedFieldStrength: Float? = null
    private var expectedVerticalFraction: Float? = null
    private var lastReading: CompassReading? = null

    var onDirectionChanged: ((CompassReading) -> Unit)? = null

    private val checkReadingAge = object : Runnable {
        override fun run() {
            if (!active) return
            val remaining = 650L - (SystemClock.elapsedRealtime() - lastReadingTime)
            if (remaining <= 0L) publish(CompassReading(filter.heading))
            handler.postDelayed(this, if (remaining > 0L) remaining else 650L)
        }
    }

    fun setMagneticReference(fieldStrength: Float, inclinationDegrees: Float) {
        expectedFieldStrength = fieldStrength
        expectedVerticalFraction = -sin(Math.toRadians(inclinationDegrees.toDouble())).toFloat()
    }

    fun registerListeners(readDisplayRotation: () -> Int): Boolean {
        unregisterListeners()
        displayRotation = readDisplayRotation
        val rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        usesRotationVector = rotation != null &&
            sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME)
        monitorsMagnetometer = magnetometer != null && sensorManager.registerListener(
            this, magnetometer, SensorManager.SENSOR_DELAY_GAME
        )
        active = usesRotationVector || (
            monitorsMagnetometer && sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            } == true
        )
        if (!active) {
            unregisterListeners()
        } else {
            lastReadingTime = SystemClock.elapsedRealtime()
            handler.postDelayed(checkReadingAge, 650L)
        }
        return active
    }

    fun unregisterListeners() {
        active = false
        handler.removeCallbacks(checkReadingAge)
        sensorManager.unregisterListener(this)
        hasAcceleration = false
        magneticTimestamp = 0L
        magneticAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE
        lastDisplayRotation = -1
        lastReading = null
        expectedFieldStrength = null
        expectedVerticalFraction = null
        compassHeading.reset()
        filter.reset()
        displayRotation = { Surface.ROTATION_0 }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!active) return
        val type = event.sensor.type
        if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            event.values.copyInto(magneticField, endIndex = 3)
            magneticTimestamp = event.timestamp
            magneticAccuracy = event.accuracy
            if (usesRotationVector) return
        } else if (type == Sensor.TYPE_ACCELEROMETER) {
            event.values.copyInto(acceleration, endIndex = 3)
            hasAcceleration = true
        }

        val reportedError = if (usesRotationVector) {
            if (type != Sensor.TYPE_ROTATION_VECTOR) return
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            event.values.getOrNull(4)?.takeIf { it.isFinite() && it >= 0f }?.let {
                Math.toDegrees(it.toDouble()).toFloat()
            }
        } else {
            if (!hasAcceleration || magneticTimestamp == 0L) return
            if (!SensorManager.getRotationMatrix(rotationMatrix, null, acceleration, magneticField)) {
                publish(CompassReading(filter.heading))
                return
            }
            null
        }

        val accuracy = if (usesRotationVector) event.accuracy else magneticAccuracy
        val rotation = displayRotation()
        if (rotation != lastDisplayRotation) updateDisplayAxes(rotation)
        val matrix = if (rotation == Surface.ROTATION_0) rotationMatrix else {
            if (!SensorManager.remapCoordinateSystem(rotationMatrix, screenX, screenY, displayMatrix)) return
            displayMatrix
        }

        val rawHeading = compassHeading.azimuth(matrix)
        if (rawHeading == null) {
            publish(CompassReading(filter.heading))
            return
        }
        lastReadingTime = SystemClock.elapsedRealtime()
        // Keep following live readings even when accuracy is too low to confirm Qibla.
        val heading = filter.update(rawHeading, event.timestamp)
        val deviation = magneticDeviation(event.timestamp)
        val reliable = accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM &&
            (reportedError == null || reportedError <= 25f) &&
            deviation != null && deviation <= 30f
        val canConfirm = reliable && deviation <= 12f && when {
            reportedError != null -> reportedError <= 12f
            else -> accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH
        }
        publish(
            CompassReading(
                heading = heading,
                rawHeading = rawHeading,
                reliable = reliable,
                canConfirm = canConfirm,
                timestamp = event.timestamp,
            )
        )
    }

    private fun magneticDeviation(timestamp: Long): Float? {
        if (!monitorsMagnetometer) return 0f
        if (magneticTimestamp == 0L || abs(timestamp - magneticTimestamp) > 200_000_000L ||
            magneticAccuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
        ) return null

        val mx = magneticField[0]
        val my = magneticField[1]
        val mz = magneticField[2]
        val strength = sqrt(mx * mx + my * my + mz * mz)
        val vertical = rotationMatrix[6] * mx + rotationMatrix[7] * my + rotationMatrix[8] * mz
        if (!isMagneticFieldUsable(strength, expectedFieldStrength, vertical / strength, expectedVerticalFraction)) {
            return null
        }
        val east = rotationMatrix[0] * mx + rotationMatrix[1] * my + rotationMatrix[2] * mz
        val north = rotationMatrix[3] * mx + rotationMatrix[4] * my + rotationMatrix[5] * mz
        return abs(Math.toDegrees(atan2(east, north).toDouble()).toFloat())
    }

    private fun updateDisplayAxes(rotation: Int) {
        filter.reset()
        lastDisplayRotation = rotation
        screenX = when (rotation) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y
            else -> SensorManager.AXIS_X
        }
        screenY = when (rotation) {
            Surface.ROTATION_90 -> SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_X
            else -> SensorManager.AXIS_Y
        }
    }

    private fun publish(reading: CompassReading) {
        if (reading == lastReading) return
        lastReading = reading
        onDirectionChanged?.invoke(reading)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) magneticAccuracy = accuracy
    }
}
