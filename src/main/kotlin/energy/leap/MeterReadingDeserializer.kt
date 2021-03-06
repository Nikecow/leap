package energy.leap

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import energy.leap.model.MeterReading
import java.io.IOException

class MeterReadingDeserializer @JvmOverloads constructor(vc: Class<*>? = null) : StdDeserializer<MeterReading>(vc) {
    companion object {
        private const val TITLE_KEY = "title"
        private const val ID_KEY = "id"
        private const val METER_INFO_KEY = "meterInfo"
        private const val INTERVAL_READINGS_KEY = "intervalReadings"
        private const val UNIT_PRICE_KEY_SUFFIX = "Price"
        private const val GENERIC_UNIT_PRICE_KEY = "unitPrice"
    }

    private val objectMapper = CustomObjectMapper()

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, context: DeserializationContext?): MeterReading {
        val root = jp.codec.readTree<JsonNode>(jp)

        val id = root.at("/id")
        val title = root.at("/title")[""]
        val readingType = root.at("/entry")[0].at("/content/ReadingType")
        val intervalReadings = root.at("/entry")[1].at("/content/IntervalBlock/IntervalReading")

        val obj = jp.createObjectNode()

        obj.setValue(ID_KEY, id)
        obj.setValue(TITLE_KEY, title)
        obj.setValue(METER_INFO_KEY, readingType.convertToGenericReadingType(jp))
        obj.setValue(INTERVAL_READINGS_KEY, intervalReadings)

        return objectMapper.getMapper().treeToValue(obj, MeterReading::class.java)
    }

    private fun JsonNode.convertToGenericReadingType(jp: JsonParser): ObjectNode {
        val obj = jp.createObjectNode()
        val it = fields()

        while (it.hasNext()) {
            val (key, value) = it.next()

            if (key.endsWith(UNIT_PRICE_KEY_SUFFIX)) {
                obj.setValue(GENERIC_UNIT_PRICE_KEY, value)
            } else {
                obj.setValue(key, value)
            }
        }
        return obj
    }

    private fun ObjectNode.setValue(fieldName: String, value: JsonNode) = set<JsonNode>(fieldName, value)
    private fun JsonParser.createObjectNode() = codec.createObjectNode() as ObjectNode

}
