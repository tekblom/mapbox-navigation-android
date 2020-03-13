package com.mapbox.navigation.core.routerefresh

import com.mapbox.api.directionsrefresh.v1.MapboxDirectionsRefresh
import com.mapbox.api.directionsrefresh.v1.models.DirectionsRefreshResponse
import retrofit2.Callback

/**
 * This class is used for adding unit tests to [RouteRefreshApi]
 */
internal class RouteRefreshRetrofit {

    internal fun enqueueCall(
        mapboxDirectionsRefresh: MapboxDirectionsRefresh,
        callback: Callback<DirectionsRefreshResponse>
    ) {
        mapboxDirectionsRefresh.enqueueCall(callback)
    }
}
