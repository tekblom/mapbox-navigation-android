package com.mapbox.navigation.core.telemetry.events

/**
 * Class that contains step meta data
 */
internal class NavigationStepData(metricsRouteProgress: MetricsRouteProgress) {
    val upcomingInstruction: String? = "Turn right onto Ridgemont Avenue"
    val upcomingModifier: String? = metricsRouteProgress.upcomingStepModifier
    val upcomingName: String? = metricsRouteProgress.upcomingStepName
    val upcomingType: String? = "turn"
    val previousInstruction: String? = "Turn right onto Gaither Road"
    val previousModifier: String? = metricsRouteProgress.previousStepModifier
    val previousName: String? = metricsRouteProgress.previousStepName
    val previousType: String? = "turn"
    val distance: Int = metricsRouteProgress.currentStepDistance
    val duration: Int = metricsRouteProgress.currentStepDuration
    val distanceRemaining: Int = metricsRouteProgress.currentStepDistanceRemaining
    val durationRemaining: Int = metricsRouteProgress.currentStepDurationRemaining
}
