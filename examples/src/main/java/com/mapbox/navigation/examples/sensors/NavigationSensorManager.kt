package com.mapbox.navigation.examples.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.mapbox.navigation.navigator.SensorMapper

class NavigationSensorManager(
    private val sensorManager: SensorManager
) : SensorEventListener {

    private var eventEmitter: (SensorEvent) -> Unit = { }

    fun start(eventEmitter: (SensorEvent) -> Unit) {
        this.eventEmitter = eventEmitter
        sensorList.forEach { sensor ->
            sensorManager.registerListener(this, sensor, 0)
        }
    }

    val sensorList: List<Sensor> by lazy {
        val supportedSensorTypes = SensorMapper.getSupportedSensorTypes()
        sensorManager.getSensorList(Sensor.TYPE_ALL)
            .filter { sensor ->
                supportedSensorTypes.contains(sensor.type)
            }
            .filterNotNull()
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        eventEmitter.invoke(event)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Haven't found a need for this
    }

    /**
     * Helper function to turn signalsPerSecond into what Android expects, samplingPeriodUs
     *
     * 25 signals per second is 40000 samplingPeriodUs.
     */
    private fun toSamplingPeriodUs(signalsPerSecond: Int): Int {
        return 1000000 / signalsPerSecond
    }
}
