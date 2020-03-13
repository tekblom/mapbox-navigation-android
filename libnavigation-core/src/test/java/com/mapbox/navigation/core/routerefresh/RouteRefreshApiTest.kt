package com.mapbox.navigation.core.routerefresh

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.navigation.base.trip.model.RouteProgress
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/**
 * These tests should fail if MapboxDirectionsRefresh api parameters change
 */
class RouteRefreshApiTest {

    private val routeRefreshRetrofit: RouteRefreshRetrofit = mockk()
    private val routeRefreshApi: RouteRefreshApi = RouteRefreshApi(routeRefreshRetrofit)

    @Test
    fun `should call route refresh with parameters`() {
        val accessToken = "pk.123"
        val directionsRoute: DirectionsRoute? = mockk {
            every { routeOptions() } returns mockk {
                every { requestUuid() } returns "test_request_id"
            }
            every { routeIndex() } returns "2"
        }
        val routeProgress: RouteProgress = mockk(relaxed = true)
        val callback: RouteRefreshCallback = mockk(relaxed = true) {
            every { onError(any()) } returns Unit
        }
        every { routeRefreshRetrofit.enqueueCall(any(), any()) } returns Unit

        routeRefreshApi.refreshRoute(accessToken, directionsRoute, routeProgress, callback)

        verify(exactly = 1) { routeRefreshRetrofit.enqueueCall(any(), any()) }
        verify(exactly = 0) { callback.onError(any()) }
    }

    @Test
    fun `should error with empty access token`() {
        val accessToken = ""
        val directionsRoute: DirectionsRoute = mockk(relaxed = true)
        val routeProgress: RouteProgress = mockk(relaxed = true)
        val callback: RouteRefreshCallback = mockk(relaxed = true) {
            every { onError(any()) } returns Unit
        }

        routeRefreshApi.refreshRoute(accessToken, directionsRoute, routeProgress, callback)

        verify(exactly = 0) { callback.onRefresh(any()) }
        verify(exactly = 1) { callback.onError(any()) }
    }

    @Test
    fun `should error with null directions route`() {
        val accessToken = "pk.123"
        val directionsRoute: DirectionsRoute? = null
        val routeProgress: RouteProgress = mockk(relaxed = true)
        val callback: RouteRefreshCallback = mockk(relaxed = true) {
            every { onError(any()) } returns Unit
        }

        routeRefreshApi.refreshRoute(accessToken, directionsRoute, routeProgress, callback)

        verify(exactly = 0) { callback.onRefresh(any()) }
        verify(exactly = 1) { callback.onError(any()) }
    }

    @Test
    fun `should error with empty request uuid`() {
        val accessToken = "pk.123"
        val directionsRoute: DirectionsRoute? = mockk {
            every { routeOptions() } returns mockk {
                every { requestUuid() } returns ""
            }
        }
        val routeProgress: RouteProgress = mockk(relaxed = true)
        val callback: RouteRefreshCallback = mockk(relaxed = true) {
            every { onError(any()) } returns Unit
        }

        routeRefreshApi.refreshRoute(accessToken, directionsRoute, routeProgress, callback)

        verify(exactly = 0) { callback.onRefresh(any()) }
        verify(exactly = 1) { callback.onError(any()) }
    }
}
