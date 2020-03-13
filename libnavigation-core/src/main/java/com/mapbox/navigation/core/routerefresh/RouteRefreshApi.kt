package com.mapbox.navigation.core.routerefresh

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directionsrefresh.v1.MapboxDirectionsRefresh
import com.mapbox.navigation.base.trip.model.RouteProgress

internal class RouteRefreshApi(
    private val routeRefreshRetrofit: RouteRefreshRetrofit
) {
    fun refreshRoute(
        accessToken: String,
        route: DirectionsRoute?,
        routeProgress: RouteProgress?,
        callback: RouteRefreshCallback
    ) {
        val refreshBuilder = MapboxDirectionsRefresh.builder()
        if (accessToken.isNotEmpty()) {
            refreshBuilder.accessToken(accessToken)
        }
        val originalRoute: DirectionsRoute = route ?: run {
            callback.onError(RouteRefreshError("No DirectionsRoute to refresh"))
            return
        }
        originalRoute.routeOptions()?.requestUuid()?.let {
            refreshBuilder.requestId(it)
        }

        val legIndex = routeProgress?.currentLegProgress()?.legIndex() ?: 0
        refreshBuilder.legIndex(legIndex)

        return try {
            val mapboxDirectionsRefresh = refreshBuilder.build()
            val callbackMapper = RouteRefreshCallbackMapper(originalRoute, legIndex, callback)
            routeRefreshRetrofit.enqueueCall(mapboxDirectionsRefresh, callbackMapper)
        } catch (throwable: Throwable) {
            callback.onError(RouteRefreshError(
                message = "Route refresh call failed",
                throwable = throwable))
        }
    }
}
