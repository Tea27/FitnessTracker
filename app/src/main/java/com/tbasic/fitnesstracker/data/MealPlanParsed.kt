package com.tbasic.fitnesstracker.data
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int

object StringOrIntSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringOrInt", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("StringOrIntSerializer works only with Json")

        val element: JsonElement = jsonDecoder.decodeJsonElement()

        return when (element) {
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content
                    element.intOrNull != null -> element.int.toString()
                    element.doubleOrNull != null -> element.double.toInt().toString()
                    else -> throw SerializationException("Unsupported JSON primitive: $element")
                }
            }
            else -> throw SerializationException("Expected primitive JSON, got $element")
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

// Extension helpers
val JsonPrimitive.intOrNull: Int?
    get() = this.intOrNullSafe()

val JsonPrimitive.doubleOrNull: Double?
    get() = this.doubleOrNullSafe()

fun JsonPrimitive.intOrNullSafe(): Int? = try {
    this.int
} catch (e: Exception) {
    null
}

fun JsonPrimitive.doubleOrNullSafe(): Double? = try {
    this.double
} catch (e: Exception) {
    null
}

@Serializable
data class MealPlanSelection(
    val days: List<DayMealPlanWithDates>
)

@Serializable
data class DayMealPlanWithDates(
    val day: String = "",
    val meals: List<MealParsed> = listOf()
)

@Serializable
data class ParsedMealPlan(
    val meals: List<MealParsed>
)

@Serializable
data class MealParsed(
    val name: String = "",
    val ingredients: List<String> = emptyList(),
    val instructions: String = "",
    @Serializable(with = StringOrIntSerializer::class)
    val calories: String = "0",
    @Serializable(with = StringOrIntSerializer::class)
    val prepTime: String = "0"
)
