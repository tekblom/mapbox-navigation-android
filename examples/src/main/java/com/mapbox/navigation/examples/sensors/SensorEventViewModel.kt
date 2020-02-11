package com.mapbox.navigation.examples.sensors

import android.app.Application
import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SensorEventViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val navigationSensorManager = NavigationSensorManager(sensorManager)
    private lateinit var sensorFileWriter: SensorFileWriter

    var eventEmitter: (suspend (SensorEvent) -> Unit) = { }

    init {
        val initJob = viewModelScope.launch {
            sensorFileWriter = SensorFileWriter.openSensorFileWriter(application)
            sensorFileWriter.write(navigationSensorManager.sensorList)
        }

        navigationSensorManager.start { event ->
            viewModelScope.launch {
                initJob.join()
                eventEmitter.invoke(event)
                sensorFileWriter.write(event)
            }
        }
    }

    override fun onCleared() {
        navigationSensorManager.stop()
        eventEmitter = { }
        sensorFileWriter.close()

        super.onCleared()
    }
}

