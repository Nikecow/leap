package energy.leap

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.IOException

class MeterReadingDeserializer @JvmOverloads constructor(vc: Class<*>? = null) : StdDeserializer<MeterReading>(vc) {

    private val objectMapper: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        .registerKotlinModule()
        .registerModule(JavaTimeModule())

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, context: DeserializationContext?): MeterReading? {
        val root = jp.codec.readTree<JsonNode>(jp)

        val id = root.at("/id")
        val title = root.at("/title")[""]
        val readingType = root.at("/entry")[0].at("/content/ReadingType")
        val intervalReadings = root.at("/entry")[1].at("/content/IntervalBlock/IntervalReading")

        val meterReadingObject = jp.createObjectNode()

        meterReadingObject.setValue("id", id)
        meterReadingObject.setValue("title", title)
        meterReadingObject.setValue("meterInfo", convertToGenericReadingType(jp, readingType))
        meterReadingObject.setValue("intervalReadings", intervalReadings)

        return objectMapper.treeToValue(meterReadingObject, MeterReading::class.java)
    }

    private fun convertToGenericReadingType(jp: JsonParser, jNode: JsonNode): ObjectNode {
        val unitPriceKeySuffix = "Price"
        val genericUnitPriceKey = "unitPrice"

        val objNode = jp.createObjectNode()

        val it = jNode.fields()
        while (it.hasNext()) {
            val pair = it.next()
            if (pair.key.endsWith(unitPriceKeySuffix)) {
                objNode.setValue(genericUnitPriceKey, pair.value)
            } else {
                objNode.setValue(pair.key, pair.value)
            }
        }
        return objNode
    }

    private fun ObjectNode.setValue(fieldName: String, value: JsonNode) = set<JsonNode>(fieldName, value)
    private fun JsonParser.createObjectNode() = codec.createObjectNode() as ObjectNode

}
