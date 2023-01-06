package center.sciprog.maps.geojson

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import center.sciprog.maps.coordinates.Gmc
import center.sciprog.maps.features.*
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive


/**
 * Add a single Json geometry to a feature builder
 */
public fun FeatureGroup<Gmc>.geoJsonGeometry(
    geometry: GeoJsonGeometry,
    id: String? = null,
): FeatureId<Feature<Gmc>> = when (geometry) {
    is GeoJsonLineString -> points(
        geometry.coordinates,
        pointMode = PointMode.Lines
    )

    is GeoJsonMultiLineString -> group(id = id) {
        geometry.coordinates.forEach {
            points(
                it,
                pointMode = PointMode.Lines
            )
        }
    }

    is GeoJsonMultiPoint -> points(
        geometry.coordinates,
        pointMode = PointMode.Points
    )

    is GeoJsonMultiPolygon -> group(id = id) {
        geometry.coordinates.forEach {
            polygon(
                it.first(),
            )
        }
    }

    is GeoJsonPoint -> circle(geometry.coordinates, id = id)
    is GeoJsonPolygon -> polygon(
        geometry.coordinates.first(),
    )

    is GeoJsonGeometryCollection -> group(id = id) {
        geometry.geometries.forEach {
            geoJsonGeometry(it)
        }
    }
}

public fun FeatureGroup<Gmc>.geoJsonFeature(
    geoJson: GeoJsonFeature,
    id: String? = null,
): FeatureId<Feature<Gmc>> {
    val geometry = geoJson.geometry ?: return group{}
    val idOverride = geoJson.json["id"]?.jsonPrimitive?.contentOrNull ?: geoJson.properties?.get("id")?.jsonPrimitive?.contentOrNull ?: id
    val colorOverride = geoJson.properties?.get("color")?.jsonPrimitive?.intOrNull?.let { Color(it) }
    val jsonGeometry =  geoJsonGeometry(geometry, idOverride)
    return if( colorOverride!= null){
        jsonGeometry.color(colorOverride)
    } else{
        jsonGeometry
    }
}

public fun FeatureGroup<Gmc>.geoJson(
    geoJson: GeoJson,
    id: String? = null,
): FeatureId<Feature<Gmc>> = when (geoJson) {
    is GeoJsonFeature -> geoJsonFeature(geoJson, id = id)
    is GeoJsonFeatureCollection -> group(id = id) {
        geoJson.features.forEach {
            geoJsonFeature(it)
        }
    }

    is GeoJsonGeometry -> geoJsonGeometry(geoJson, id = id)
}