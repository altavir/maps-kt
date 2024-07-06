package space.kscience.maps.features

import space.kscience.attributes.Attributes
import kotlin.jvm.JvmName


public fun <T : Any> FeatureBuilder<T>.draggableLine(
    aId: FeatureRef<T, MarkerFeature<T>>,
    bId: FeatureRef<T, MarkerFeature<T>>,
    id: String? = null,
): FeatureRef<T, LineFeature<T>> {
    var lineId: FeatureRef<T, LineFeature<T>>? = null

    fun drawLine(): FeatureRef<T, LineFeature<T>> {
        val currentId = feature(
            lineId?.id ?: id,
            LineFeature(
                space,
                aId.resolve().center,
                bId.resolve().center,
                Attributes<FeatureGroup<T>> {
                    ZAttribute(-10f)
                    lineId?.attributes?.let { putAll(it) }
                }
            )
        )
        lineId = currentId
        return currentId
    }

    aId.draggable { _, _ ->
        drawLine()
    }

    bId.draggable { _, _ ->
        drawLine()
    }

    return drawLine()
}

public fun <T : Any> FeatureBuilder<T>.draggableMultiLine(
    points: List<FeatureRef<T, MarkerFeature<T>>>,
    id: String? = null,
): FeatureRef<T, MultiLineFeature<T>> {
    var polygonId: FeatureRef<T, MultiLineFeature<T>>? = null

    fun drawLines(): FeatureRef<T, MultiLineFeature<T>> {
        val currentId = feature(
            polygonId?.id ?: id,
            MultiLineFeature(
                space,
                points.map { it.resolve().center },
                Attributes<FeatureGroup<T>>{
                    ZAttribute(-10f)
                    polygonId?.attributes?.let { putAll(it) }
                }
            )
        )
        polygonId = currentId
        return currentId
    }

    points.forEach {
        it.draggable { _, _ ->
            drawLines()
        }
    }

    return drawLines()
}

@JvmName("draggableMultiLineFromPoints")
public fun <T : Any> FeatureBuilder<T>.draggableMultiLine(
    points: List<T>,
    id: String? = null,
): FeatureRef<T, MultiLineFeature<T>> {
    val pointRefs = points.map {
        circle(it)
    }
    return draggableMultiLine(pointRefs, id)
}