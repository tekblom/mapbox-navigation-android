package com.mapbox.navigation.base.options

import com.mapbox.navigation.base.formatter.DistanceFormatter
import com.mapbox.navigation.base.typedef.NONE_SPECIFIED
import com.mapbox.navigation.base.typedef.ROUNDING_INCREMENT_FIFTY
import com.mapbox.navigation.base.typedef.RoundingIncrement
import com.mapbox.navigation.base.typedef.TimeFormatType

const val DEFAULT_NAVIGATOR_POLLING_DELAY = 1500L
const val DEFAULT_FASTER_ROUTE_DETECTOR_INTERVAL = 2 * 60 * 1000L // 2 minutes

data class NavigationOptions constructor(
    @RoundingIncrement val roundingIncrement: Int,
    @TimeFormatType val timeFormatType: Int,
    val navigatorPollingDelay: Long,
    val fasterRouteDetectorInterval: Long,
    val distanceFormatter: DistanceFormatter?,
    val onboardRouterConfig: MapboxOnboardRouterConfig?,
    val isFromNavigationUi: Boolean = false,
    val isDebugLoggingEnabled: Boolean = false
) {

    /**
     * Get a builder to customize a subset of current options.
     */
    fun toBuilder() = Builder(
        roundingIncrement,
        timeFormatType,
        navigatorPollingDelay,
        fasterRouteDetectorInterval,
        distanceFormatter,
        onboardRouterConfig,
        isDebugLoggingEnabled
    )

    data class Builder(
        private var roundingIncrement: Int = ROUNDING_INCREMENT_FIFTY,
        private var timeFormatType: Int = NONE_SPECIFIED,
        private var navigatorPollingDelay: Long = DEFAULT_NAVIGATOR_POLLING_DELAY,
        private var fasterRouteDetectorInterval: Long = DEFAULT_FASTER_ROUTE_DETECTOR_INTERVAL,
        private var distanceFormatter: DistanceFormatter? = null,
        private var onboardRouterConfig: MapboxOnboardRouterConfig? = null,
        private var isFromNavigationUi: Boolean = false,
        private var isDebugLoggingEnabled: Boolean = false
    ) {

        fun roundingIncrement(roundingIncrement: Int) =
            apply { this.roundingIncrement = roundingIncrement }

        fun timeFormatType(type: Int) =
            apply { this.timeFormatType = type }

        fun navigatorPollingDelay(pollingDelay: Long) =
            apply { navigatorPollingDelay = pollingDelay }

        fun fasterRouteDetectorInterval(interval: Long) =
            apply { fasterRouteDetectorInterval = interval }

        fun distanceFormatter(distanceFormatter: DistanceFormatter?) =
            apply { this.distanceFormatter = distanceFormatter }

        fun onboardRouterConfig(onboardRouterConfig: MapboxOnboardRouterConfig?) =
            apply { this.onboardRouterConfig = onboardRouterConfig }

        fun isFromNavigationUi(flag: Boolean) =
            apply { this.isFromNavigationUi = flag }

        fun isDebugLoggingEnabled(flag: Boolean) =
            apply { this.isDebugLoggingEnabled = flag }

        fun build(): NavigationOptions {
            return NavigationOptions(
                roundingIncrement,
                timeFormatType,
                navigatorPollingDelay,
                fasterRouteDetectorInterval,
                distanceFormatter,
                onboardRouterConfig,
                isFromNavigationUi,
                isDebugLoggingEnabled
            )
        }
    }
}
