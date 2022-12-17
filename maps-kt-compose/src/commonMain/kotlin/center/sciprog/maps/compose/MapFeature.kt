package center.sciprog.maps.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import center.sciprog.maps.coordinates.*
import kotlin.math.floor

public interface MapFeature {
    public interface Attribute<T>

    public val zoomRange: IntRange

    public var attributes: AttributeMap

    public fun getBoundingBox(zoom: Double): GmcRectangle?
}

public interface SelectableMapFeature : MapFeature {
    public operator fun contains(point: MapViewPoint): Boolean = getBoundingBox(point.zoom)?.let {
        point.focus in it
    } ?: false
}

public interface DraggableMapFeature : SelectableMapFeature {
    public fun withCoordinates(newCoordinates: GeodeticMapCoordinates): MapFeature
}

public fun Iterable<MapFeature>.computeBoundingBox(zoom: Double): GmcRectangle? =
    mapNotNull { it.getBoundingBox(zoom) }.wrapAll()

public fun Pair<Number, Number>.toCoordinates(): GeodeticMapCoordinates =
    GeodeticMapCoordinates.ofDegrees(first.toDouble(), second.toDouble())

internal val defaultZoomRange = 1..18

/**
 * A feature that decides what to show depending on the zoom value (it could change size of shape)
 */
public class MapFeatureSelector(
    override var attributes: AttributeMap = AttributeMap(),
    public val selector: (zoom: Int) -> MapFeature,
) : MapFeature {
    override val zoomRange: IntRange get() = defaultZoomRange

    override fun getBoundingBox(zoom: Double): GmcRectangle? = selector(floor(zoom).toInt()).getBoundingBox(zoom)
}

public class MapDrawFeature(
    public val position: GeodeticMapCoordinates,
    override val zoomRange: IntRange = defaultZoomRange,
    override var attributes: AttributeMap = AttributeMap(),
    public val drawFeature: DrawScope.() -> Unit,
) : DraggableMapFeature {
    override fun getBoundingBox(zoom: Double): GmcRectangle {
        //TODO add box computation
        return GmcRectangle(position, position)
    }

    override fun withCoordinates(newCoordinates: GeodeticMapCoordinates): MapFeature =
        MapDrawFeature(newCoordinates, zoomRange, attributes, drawFeature)
}

public class MapPathFeature(
    public val rectangle: GmcRectangle,
    public val path: Path,
    public val brush: Brush,
    public val style: DrawStyle = Fill,
    public val targetRect: Rect = path.getBounds(),
    override val zoomRange: IntRange = defaultZoomRange,
    override var attributes: AttributeMap = AttributeMap(),
) : DraggableMapFeature {
    override fun withCoordinates(newCoordinates: GeodeticMapCoordinates): MapFeature =
        MapPathFeature(rectangle.moveTo(newCoordinates), path, brush, style, targetRect, zoomRange)

    override fun getBoundingBox(zoom: Double): GmcRectangle = rectangle

}

public class MapPointsFeature(
    public val points: List<GeodeticMapCoordinates>,
    override val zoomRange: IntRange = defaultZoomRange,
    public val stroke: Float = 2f,
    public val color: Color = Color.Red,
    public val pointMode: PointMode = PointMode.Points,
    override var attributes: AttributeMap = AttributeMap(),
) : MapFeature {
    override fun getBoundingBox(zoom: Double): GmcRectangle = GmcRectangle(points.first(), points.last())
}

public data class MapCircleFeature(
    public val center: GeodeticMapCoordinates,
    override val zoomRange: IntRange = defaultZoomRange,
    public val size: Float = 5f,
    public val color: Color = Color.Red,
    override var attributes: AttributeMap = AttributeMap(),
) : DraggableMapFeature {
    override fun getBoundingBox(zoom: Double): GmcRectangle {
        val scale = WebMercatorProjection.scaleFactor(zoom)
        return GmcRectangle.square(center, (size / scale).radians, (size / scale).radians)
    }

    override fun withCoordinates(newCoordinates: GeodeticMapCoordinates): MapFeature =
        MapCircleFeature(newCoordinates, zoomRange, size, color, attributes)
}

public class MapRectangleFeature(
    public val center: GeodeticMapCoordinates,
    override val zoomRange: IntRange = defaultZoomRange,
    public val size: DpSize = DpSize(5.dp, 5.dp),
    public val color: Color = Color.Red,
    override var attributes: AttributeMap = AttributeMap(),
) : DraggableMapFeature {
    override fun getBoundingBox(zoom: Double): GmcRectangle {
        val scale = WebMercatorProjection.scaleFactor(zoom)
        return GmcRectangle.square(center, (size.height.value / scale).radians, (size.width.value / scale).radians)
    }

    override fun withCoordinates(newCoordinates: GeodeticMapCoordinates): MapFeature =
        MapRectangleFeature(newCoordinates, zoomRange, size, color, attributes)
}

public class MapLineFeature(
    public val a: GeodeticMapCoordinates,
    public val b: GeodeticMapCoordinates,
    override val zoomRange: IntRange = defaultZoomRange,
    public val color: Color = Color.Red,
    override var attributes: AttributeMap = AttributeMap(),
) : SelectableMapFeature {
    override fun getBoundingBox(zoom: Double): GmcRectangle = GmcRectangle(a, b)

    override fun contains(point: MapViewPoint): Boolean {
        return super.contains(point)
    }
}

/**
 * @param startAngle the angle from parallel downwards for the start of the arc
 * @param arcLength arc length
 */
public class MapArcFeature(
    public val oval: GmcRectangle,
    public val startAngle: Angle,
    public val arcLength: Angle,
    override val zoomRange: IntRange = defaultZoomRange,
    public val color: Color = Color.Red,
    override var attributes: AttributeMap = AttributeMap(),
) : DraggableMapFeature {
    override fun getBoundingBox(zoom: Double): GmcRectangle = oval

    override fun withCoordinates(newCoordinates: GeodeticMapCoordinates): MapFeature =
        MapArcFeature(oval.moveTo(newCoordinates), startAngle, arcLength, zoomRange, color, attributes)
}

public class MapBitmapImageFeature(
    public val position: GeodeticMapCoordinates,
    public val image: ImageBitmap,
    public val size: IntSize = IntSize(15, 15),
    override val zoomRange: IntRange = defaultZoomRange,
    override var attributes: AttributeMap = AttributeMap(),
) : DraggableMapFeature {
    override fun getBoundingBox(zoom: Double): GmcRectangle = GmcRectangle(position, position)

    override fun withCoordinates(newCoordinates: GeodeticMapCoordinates): MapFeature =
        MapBitmapImageFeature(newCoordinates, image, size, zoomRange, attributes)
}

public class MapVectorImageFeature(
    public val position: GeodeticMapCoordinates,
    public val image: ImageVector,
    public val size: DpSize = DpSize(20.dp, 20.dp),
    override val zoomRange: IntRange = defaultZoomRange,
    override var attributes: AttributeMap = AttributeMap(),
) : DraggableMapFeature {
    override fun getBoundingBox(zoom: Double): GmcRectangle = GmcRectangle(position, position)

    override fun withCoordinates(newCoordinates: GeodeticMapCoordinates): MapFeature =
        MapVectorImageFeature(newCoordinates, image, size, zoomRange, attributes)

    @Composable
    public fun painter(): VectorPainter = rememberVectorPainter(image)
}

/**
 * A group of other features
 */
public class MapFeatureGroup(
    public val children: Map<FeatureId<*>, MapFeature>,
    override val zoomRange: IntRange = defaultZoomRange,
    override var attributes: AttributeMap = AttributeMap(),
) : MapFeature {
    override fun getBoundingBox(zoom: Double): GmcRectangle? =
        children.values.mapNotNull { it.getBoundingBox(zoom) }.wrapAll()
}

public class MapTextFeature(
    public val position: GeodeticMapCoordinates,
    public val text: String,
    override val zoomRange: IntRange = defaultZoomRange,
    public val color: Color = Color.Black,
    override var attributes: AttributeMap = AttributeMap(),
    public val fontConfig: MapTextFeatureFont.() -> Unit,
) : DraggableMapFeature {
    override fun getBoundingBox(zoom: Double): GmcRectangle = GmcRectangle(position, position)

    override fun withCoordinates(newCoordinates: GeodeticMapCoordinates): MapFeature =
        MapTextFeature(newCoordinates, text, zoomRange, color, attributes, fontConfig)
}
