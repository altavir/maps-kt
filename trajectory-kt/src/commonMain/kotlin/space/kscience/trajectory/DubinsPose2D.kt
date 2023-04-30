/*
 * Copyright 2018-2022 KMath contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:UseSerializers(Euclidean2DSpace.VectorSerializer::class)

package space.kscience.trajectory

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import space.kscience.kmath.geometry.*
import kotlin.math.atan2

/**
 * Combination of [Vector] and its view angle (clockwise from positive y-axis direction)
 */
@Serializable(DubinsPose2DSerializer::class)
public interface DubinsPose2D : DoubleVector2D {
    public val coordinates: DoubleVector2D
    public val bearing: Angle

    /**
     * Reverse the direction of this pose to the opposite, keeping other parameters the same
     */
    public fun reversed(): DubinsPose2D

    public companion object {
        public fun bearingToVector(bearing: Angle): Vector2D<Double> =
            Euclidean2DSpace.vector(cos(bearing), sin(bearing))

        public fun vectorToBearing(vector2D: DoubleVector2D): Angle {
            require(vector2D.x != 0.0 || vector2D.y != 0.0) { "Can't get bearing of zero vector" }
            return atan2(vector2D.y, vector2D.x).radians
        }
    }
}


@Serializable
public class PhaseVector2D(
    override val coordinates: DoubleVector2D,
    public val velocity: DoubleVector2D,
) : DubinsPose2D, DoubleVector2D by coordinates {
    override val bearing: Angle get() = atan2(velocity.x, velocity.y).radians

    override fun reversed(): DubinsPose2D = with(Euclidean2DSpace) { PhaseVector2D(coordinates, -velocity) }
}

@Serializable
@SerialName("DubinsPose2D")
private class DubinsPose2DImpl(
    override val coordinates: DoubleVector2D,
    override val bearing: Angle,
) : DubinsPose2D, DoubleVector2D by coordinates {

    override fun reversed(): DubinsPose2D = DubinsPose2DImpl(coordinates, bearing.plus(Angle.pi).normalized())

    override fun toString(): String = "Pose2D(x=$x, y=$y, bearing=$bearing)"
}

public object DubinsPose2DSerializer : KSerializer<DubinsPose2D> {
    private val proxySerializer = DubinsPose2DImpl.serializer()

    override val descriptor: SerialDescriptor
        get() = proxySerializer.descriptor

    override fun deserialize(decoder: Decoder): DubinsPose2D {
        return decoder.decodeSerializableValue(proxySerializer)
    }

    override fun serialize(encoder: Encoder, value: DubinsPose2D) {
        val pose = value as? DubinsPose2DImpl ?: DubinsPose2DImpl(value.coordinates, value.bearing)
        encoder.encodeSerializableValue(proxySerializer, pose)
    }
}

public fun DubinsPose2D(coordinate: DoubleVector2D, bearing: Angle): DubinsPose2D =
    DubinsPose2DImpl(coordinate, bearing)

public fun DubinsPose2D(point: DoubleVector2D, direction: DoubleVector2D): DubinsPose2D =
    DubinsPose2D(point, DubinsPose2D.vectorToBearing(direction))

public fun DubinsPose2D(x: Number, y: Number, bearing: Angle): DubinsPose2D =
    DubinsPose2DImpl(Euclidean2DSpace.vector(x, y), bearing)