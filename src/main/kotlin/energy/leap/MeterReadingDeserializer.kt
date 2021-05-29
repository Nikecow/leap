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
    private val TITLE_KEY = "title"
    private val ID_KEY = "id"
    private val METER_INFO_KEY = "meterInfo"
    private val INTERVAL_READINGS_KEY = "intervalReadings"
    private val UNIT_PRICE_KEY_SUFFIX = "Price"
    private val GENERIC_UNIT_PRICE_KEY = "unitPrice"

    private val objectMapper = CustomObjectMapper()

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, context: DeserializationContext?): MeterReading {
        val root = jp.codec.readTree<JsonNode>(jp)

        val id = root.at("/id")
        val title = root.at("/title")[""]
        val readingType = root.at("/entry")[0].at("/content/ReadingType")
        val intervalReadings = root.at("/entry")[1].at("/content/IntervalBlock/IntervalReading")

        val meterReadingObject = jp.createObjectNode()

        meterReadingObject.setValue(ID_KEY, id)
        meterReadingObject.setValue(TITLE_KEY, title)
        meterReadingObject.setValue(METER_INFO_KEY, readingType.convertToGenericReadingType(jp))
        meterReadingObject.setValue(INTERVAL_READINGS_KEY, intervalReadings)

        return objectMapper.getMapper().treeToValue(meterReadingObject, MeterReading::class.java)
    }

    private fun JsonNode.convertToGenericReadingType(jp: JsonParser): ObjectNode {

        val objNode = jp.createObjectNode()

        val it = fields()
        while (it.hasNext()) {
            val (key, value) = it.next()

            if (key.endsWith(UNIT_PRICE_KEY_SUFFIX)) {
                objNode.setValue(GENERIC_UNIT_PRICE_KEY, value)
            } else {
                objNode.setValue(key, value)
            }
        }
        return objNode
    }

    private fun ObjectNode.setValue(fieldName: String, value: JsonNode) = set<JsonNode>(fieldName, value)
    private fun JsonParser.createObjectNode() = codec.createObjectNode() as ObjectNode

}
