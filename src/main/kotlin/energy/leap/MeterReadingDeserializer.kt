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
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.IOException

class MeterReadingDeserializer @JvmOverloads constructor(vc: Class<*>? = null) : StdDeserializer<MeterReading>(vc) {

    private val objectMapper: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        .registerKotlinModule()

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, context: DeserializationContext?): MeterReading? {
        val root = jp.codec.readTree<JsonNode>(jp)

        val id = root.at("/id")
        val title = root.at("/title")[""]
        val readingType = root.at("/entry/content/ReadingType")

        val newRoot = jp.codec.createObjectNode() as ObjectNode

        newRoot.set<JsonNode>("id", id)
        newRoot.set<JsonNode>("title", title)
        newRoot.set<JsonNode>("meterInfo", readingType)

        val meterReading = objectMapper.treeToValue(newRoot, MeterReading::class.java)

        return meterReading
    }
}
