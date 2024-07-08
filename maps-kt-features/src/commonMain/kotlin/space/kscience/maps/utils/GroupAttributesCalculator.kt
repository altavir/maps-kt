package space.kscience.maps.utils

import space.kscience.attributes.Attributes
import space.kscience.attributes.plus
import space.kscience.maps.features.Feature

public class GroupAttributesCalculator<T : Any>(
    private val features: Map<String, Feature<T>>,
    private val attributesCache: MutableMap<List<String>, Attributes> = mutableMapOf()
) {
    public fun computeGroupAttributes(path: List<String>): Attributes = attributesCache.getOrPut(path){
        if (path.isEmpty()) return Attributes.EMPTY
        else if (path.size == 1) {
            features[path.first()]?.attributes ?: Attributes.EMPTY
        } else {
            computeGroupAttributes(path.dropLast(1)) + (features[path.first()]?.attributes ?: Attributes.EMPTY)
        }
    }
}
