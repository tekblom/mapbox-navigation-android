package com.mapbox.navigation.core.telemetry.events

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.mapbox.navigation.base.metrics.NavigationMetrics

@Keep
@SuppressLint("ParcelCreator")
internal class NavigationDepartEvent(
    phoneState: PhoneState
) : NavigationEvent(phoneState) {

    override fun getEventName(): String = NavigationMetrics.DEPART
}
