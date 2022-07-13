package centre.sciprog.maps

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Observable position on the map. Includes observation coordinate and [zoom] factor
 */
data class MapViewPoint(
    val focus: GeodeticMapCoordinates,
    val zoom: Double,
) {
    val scaleFactor by lazy { WebMercatorProjection.scaleFactor(zoom.roundToInt()) }
}

fun MapViewPoint.move(deltaX: Double, deltaY: Double): MapViewPoint {
    val newCoordinates = GeodeticMapCoordinates.ofRadians(
        (focus.latitude + deltaY / scaleFactor).coerceIn(
            -MercatorProjection.MAXIMUM_LATITUDE,
            MercatorProjection.MAXIMUM_LATITUDE
        ),
        focus.longitude + deltaX / scaleFactor
    )
    return MapViewPoint(newCoordinates, zoom)
}

fun MapViewPoint.move(delta: GeodeticMapCoordinates): MapViewPoint {
    val newCoordinates = GeodeticMapCoordinates.ofRadians(
        (focus.latitude + delta.latitude).coerceIn(
            -MercatorProjection.MAXIMUM_LATITUDE,
            MercatorProjection.MAXIMUM_LATITUDE
        ),
        focus.longitude + delta.longitude
    )
    return MapViewPoint(newCoordinates, zoom)
}

fun MapViewPoint.zoom(zoomDelta: Double): MapViewPoint = copy(zoom = (zoom + zoomDelta).coerceIn(2.0, 18.0))

fun MapViewPoint.zoom(zoomDelta: Double, invariant: GeodeticMapCoordinates): MapViewPoint {
    val difScale = 2.0.pow(-zoomDelta)
    val newCenter = GeodeticMapCoordinates.ofRadians(
        focus.latitude + (invariant.latitude - focus.latitude) * difScale,
        focus.longitude + (invariant.longitude - focus.longitude) * difScale
    )
    return MapViewPoint(newCenter, (zoom + zoomDelta).coerceIn(2.0, 18.0))
}