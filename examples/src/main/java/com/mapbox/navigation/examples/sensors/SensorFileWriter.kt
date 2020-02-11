package com.mapbox.navigation.examples.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.os.Build
import android.os.SystemClock
import com.mapbox.navigation.navigator.SensorMapper
import com.mapbox.navigator.NavigatorSensorData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SensorFileWriter private constructor(
    private val fileDirectory: File,
    private val fileOutputStream: FileOutputStream
) {

    private val bufferedWriter: BufferedWriter = fileOutputStream.bufferedWriter()

    /**
     * Create a file with more details about each sensor. Note that this
     * is a single operation so you don't need to close() the file streams.
     */
    suspend fun write(sensorList: List<Sensor>) = withContext(Dispatchers.IO) {
        val file = File(fileDirectory, sensorFileName)
        Timber.i("$sensorFileName ${file.length()}")
        file.outputStream()
            .use { fos ->
                val bw = fos.bufferedWriter()
                sensorList.onEach {
                    val sensorType = it.name
                    bw.write("$sensorType $it")
                    bw.newLine()
                }
                bw.flush()
            }
    }

    /**
     * Write each sensor event to an open file stream.
     * Stop writing events after {@link #close()} has been called
     */
    suspend fun write(sensorEvent: SensorEvent) = withContext(Dispatchers.IO) {
        SensorMapper.toNavigatorSensorData(sensorEvent)
            ?.mapToEventRow()
            ?.let { eventRow ->
                bufferedWriter.write(eventRow)
                bufferedWriter.flush()
            }
    }

    /**
     * Closes the file stream. Stop calling write(sensorEvent)
     */
    fun close() {
        fileOutputStream.close()
    }

    private fun NavigatorSensorData.mapToEventRow(): String {
        val valuesColumn = values.joinToString(",")
        val recordedTime = currentBootNanoTime()
        return "$recordedTime $sensorType  ${timestamp.time} $elapsedTimeNanos $valuesColumn\n"
    }

    private fun currentBootNanoTime(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            SystemClock.elapsedRealtimeNanos()
        } else {
            System.nanoTime()
        }
    }

    companion object {
        private const val directoryName = "drive_data"
        private const val dataFileNamePrefix = "navigator_sensor_data"
        private const val sensorFileName = "device_sensors"
        private val formatter = SimpleDateFormat("yyy_MM_dd_HH_mm_ss", Locale.ENGLISH)

        suspend fun openSensorFileWriter(context: Context): SensorFileWriter = withContext(Dispatchers.IO) {
            val directory = File(context.filesDir, directoryName)
            directory.mkdir()

            val now = Date()
            val fileName = String.format("${dataFileNamePrefix}_%s.txt", formatter.format(now))
            val file = File(directory, fileName)
            val fileOutputStream = file.outputStream()
            Timber.i("freeSpace=${directory.freeSpace}")
            Timber.i("usableSpace=${directory.usableSpace}")
            Timber.i("directorySizeBytes=${directory.directorySizeBytes()}")
            return@withContext SensorFileWriter(directory, fileOutputStream)
        }
    }
}

private fun File.directorySizeBytes(): Long {
    var sum = 0L
    val listFiles = this.listFiles() ?: return sum
    for (element: File in listFiles) {
        sum += element.length()
    }
    return sum
}
