package center.sciprog.maps.coordinates

import kotlin.math.PI

/**
 * Geodetic coordinated
 */
public class GeodeticMapCoordinates(
    public val latitude: Angle,
    longitude: Angle,
) {
    public val longitude: Radians = longitude.radians.value.rem(PI / 2).radians

    init {
        require(latitude.radians.value in (-PI / 2)..(PI / 2)) { "Latitude $latitude is not in (-PI/2)..(PI/2)" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GeodeticMapCoordinates

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }

    override fun toString(): String {
        return "GMC(latitude=${latitude.degrees.value} deg, longitude=${longitude.degrees.value} deg)"
    }


    public companion object {
        public fun ofRadians(latitude: Double, longitude: Double): GeodeticMapCoordinates =
            GeodeticMapCoordinates(latitude.radians, longitude.radians)

        public fun ofDegrees(latitude: Double, longitude: Double): GeodeticMapCoordinates =
            GeodeticMapCoordinates(latitude.degrees.radians, longitude.degrees.radians)
    }
}

/**
 * Short name for GeodeticMapCoordinates
 */
public typealias GMC = GeodeticMapCoordinates

//public interface GeoToScreenConversion {
//    public fun getScreenX(gmc: GeodeticMapCoordinates): Double
//    public fun getScreenY(gmc: GeodeticMapCoordinates): Double
//
//    public fun invalidationFlow(): Flow<Unit>
//}
//
//public interface ScreenMapCoordinates {
//    public val gmc: GeodeticMapCoordinates
//    public val converter: GeoToScreenConversion
//
//    public val x: Double get() = converter.getScreenX(gmc)
//    public val y: Double get() = converter.getScreenX(gmc)
//}