package com.mapbox.navigation.core.telemetry.events

import androidx.annotation.Keep

/**
 * Class that contains step meta data
 */
@Keep
internal class NavigationStepData(metricsRouteProgress: MetricsRouteProgress) {
    // TODO Fix hardcoded
    val upcomingInstruction: String? = "Turn right onto Ridgemont Avenue" // Schema minLength 1
    val upcomingModifier: String? = metricsRouteProgress.upcomingStepModifier
    val upcomingName: String? = metricsRouteProgress.upcomingStepName
    // TODO Fix hardcoded
    val upcomingType: String? = "turn" // Schema minLength 1
    // TODO Fix hardcoded
    val previousInstruction: String? = "Turn right onto Gaither Road" // Schema minLength 1
    val previousModifier: String? = metricsRouteProgress.previousStepModifier
    val previousName: String? = metricsRouteProgress.previousStepName
    // TODO Fix hardcoded
    val previousType: String? = "turn" // Schema minLength 1
    val distance: Int = metricsRouteProgress.currentStepDistance
    val duration: Int = metricsRouteProgress.currentStepDuration
    val distanceRemaining: Int = metricsRouteProgress.currentStepDistanceRemaining
    val durationRemaining: Int = metricsRouteProgress.currentStepDurationRemaining
}
