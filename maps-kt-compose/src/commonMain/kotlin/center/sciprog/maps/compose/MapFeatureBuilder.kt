package center.sciprog.maps.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import center.sciprog.maps.coordinates.*

public typealias FeatureId = String

public interface MapFeatureAttributeKey<T>


public class MapFeatureAttributeSet(private val map: Map<MapFeatureAttributeKey<*>, *>) {
    public operator fun <T> get(key: MapFeatureAttributeKey<*>): T? = map[key]?.let {
        @Suppress("UNCHECKED_CAST")
        it as T
    }
}

public interface MapFeatureBuilder {
    public fun addFeature(id: FeatureId?, feature: MapFeature): FeatureId

    public fun <T> setAttribute(id: FeatureId, key: MapFeatureAttributeKey<T>, value: T)

    public val features: MutableMap<FeatureId, MapFeature>

    public fun attributes(): Map<FeatureId, MapFeatureAttributeSet>

    //TODO use context receiver for that
    public fun FeatureId.draggable(enabled: Boolean = true) {
        setAttribute(this, DraggableAttribute, enabled)
    }
}

internal class MapFeatureBuilderImpl(
    override val features: SnapshotStateMap<FeatureId, MapFeature>,
) : MapFeatureBuilder {

    private val attributes = SnapshotStateMap<FeatureId, SnapshotStateMap<MapFeatureAttributeKey<out Any?>, in Any?>>()


    private fun generateID(feature: MapFeature): FeatureId = "@feature[${feature.hashCode().toUInt()}]"

    override fun addFeature(id: FeatureId?, feature: MapFeature): FeatureId {
        val safeId = id ?: generateID(feature)
        features[id ?: generateID(feature)] = feature
        return safeId
    }

    override fun <T> setAttribute(id: FeatureId, key: MapFeatureAttributeKey<T>, value: T) {
        attributes.getOrPut(id) { SnapshotStateMap() }[key] = value
    }

    override fun attributes(): Map<FeatureId, MapFeatureAttributeSet> =
        attributes.mapValues { MapFeatureAttributeSet(it.value) }

}

public fun MapFeatureBuilder.circle(
    center: GeodeticMapCoordinates,
    zoomRange: IntRange = defaultZoomRange,
    size: Float = 5f,
    color: Color = Color.Red,
    id: FeatureId? = null,
): FeatureId = addFeature(
    id, MapCircleFeature(center, zoomRange, size, color)
)

public fun MapFeatureBuilder.circle(
    centerCoordinates: Pair<Double, Double>,
    zoomRange: IntRange = defaultZoomRange,
    size: Float = 5f,
    color: Color = Color.Red,
    id: FeatureId? = null,
): FeatureId = addFeature(
    id, MapCircleFeature(centerCoordinates.toCoordinates(), zoomRange, size, color)
)

public fun MapFeatureBuilder.rectangle(
    centerCoordinates: Pair<Double, Double>,
    zoomRange: IntRange = defaultZoomRange,
    size: DpSize = DpSize(5.dp, 5.dp),
    color: Color = Color.Red,
    id: FeatureId? = null,
): FeatureId = addFeature(
    id, MapRectangleFeature(centerCoordinates.toCoordinates(), zoomRange, size, color)
)

public fun MapFeatureBuilder.draw(
    position: Pair<Double, Double>,
    zoomRange: IntRange = defaultZoomRange,
    id: FeatureId? = null,
    drawFeature: DrawScope.() -> Unit,
): FeatureId = addFeature(id, MapDrawFeature(position.toCoordinates(), zoomRange, drawFeature))

public fun MapFeatureBuilder.line(
    aCoordinates: Gmc,
    bCoordinates: Gmc,
    zoomRange: IntRange = defaultZoomRange,
    color: Color = Color.Red,
    id: FeatureId? = null,
): FeatureId = addFeature(
    id,
    MapLineFeature(aCoordinates, bCoordinates, zoomRange, color)
)

public fun MapFeatureBuilder.line(
    curve: GmcCurve,
    zoomRange: IntRange = defaultZoomRange,
    color: Color = Color.Red,
    id: FeatureId? = null,
): FeatureId = addFeature(
    id,
    MapLineFeature(curve.forward.coordinates, curve.backward.coordinates, zoomRange, color)
)

public fun MapFeatureBuilder.line(
    aCoordinates: Pair<Double, Double>,
    bCoordinates: Pair<Double, Double>,
    zoomRange: IntRange = defaultZoomRange,
    color: Color = Color.Red,
    id: FeatureId? = null,
): FeatureId = addFeature(
    id,
    MapLineFeature(aCoordinates.toCoordinates(), bCoordinates.toCoordinates(), zoomRange, color)
)

public fun MapFeatureBuilder.arc(
    oval: GmcRectangle,
    startAngle: Angle,
    endAngle: Angle,
    zoomRange: IntRange = defaultZoomRange,
    color: Color = Color.Red,
    id: FeatureId? = null,
): FeatureId = addFeature(
    id,
    MapArcFeature(oval, startAngle, endAngle, zoomRange, color)
)

public fun MapFeatureBuilder.arc(
    center: Pair<Double, Double>,
    radius: Distance,
    startAngle: Number,
    endAngle: Number,
    zoomRange: IntRange = defaultZoomRange,
    color: Color = Color.Red,
    id: FeatureId? = null,
): FeatureId = addFeature(
    id,
    MapArcFeature(
        GmcRectangle.square(center.toCoordinates(), radius, radius),
        startAngle.degrees,
        endAngle.degrees,
        zoomRange,
        color
    )
)

public fun MapFeatureBuilder.points(
    points: List<Gmc>,
    zoomRange: IntRange = defaultZoomRange,
    stroke: Float = 2f,
    color: Color = Color.Red,
    pointMode: PointMode = PointMode.Points,
    id: FeatureId? = null,
): FeatureId = addFeature(id, MapPointsFeature(points, zoomRange, stroke, color, pointMode))

@Composable
public fun MapFeatureBuilder.image(
    position: Pair<Double, Double>,
    image: ImageVector,
    size: DpSize = DpSize(20.dp, 20.dp),
    zoomRange: IntRange = defaultZoomRange,
    id: FeatureId? = null,
): FeatureId = addFeature(id, MapVectorImageFeature(position.toCoordinates(), image, size, zoomRange))

public fun MapFeatureBuilder.group(
    zoomRange: IntRange = defaultZoomRange,
    id: FeatureId? = null,
    builder: MapFeatureBuilder.() -> Unit,
): FeatureId {
    val map = MapFeatureBuilderImpl(mutableStateMapOf()).apply(builder).features
    val feature = MapFeatureGroup(map, zoomRange)
    return addFeature(id, feature)
}

public fun MapFeatureBuilder.text(
    position: GeodeticMapCoordinates,
    text: String,
    zoomRange: IntRange = defaultZoomRange,
    color: Color = Color.Red,
    font: MapTextFeatureFont.() -> Unit = { size = 16f },
    id: FeatureId? = null,
): FeatureId = addFeature(id, MapTextFeature(position, text, zoomRange, color, font))

public fun MapFeatureBuilder.text(
    position: Pair<Double, Double>,
    text: String,
    zoomRange: IntRange = defaultZoomRange,
    color: Color = Color.Red,
    font: MapTextFeatureFont.() -> Unit = { size = 16f },
    id: FeatureId? = null,
): FeatureId = addFeature(id, MapTextFeature(position.toCoordinates(), text, zoomRange, color, font))
