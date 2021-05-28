package energy.leap

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.IOException
import java.util.UUID

//internal class CustomMeterDeserializer  : StdDeserializer<MeterReading>(MeterReading::class.java) {
//    @Throws(IOException::class, JsonProcessingException::class)
//    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): MeterReading {
//      val node: JsonNode = jp.codec.readTree(jp)
////        val id = (node["id"] as IntNode).numberValue() as Int
////        val itemName = node["itemName"].asText()
////        val userId = (node["createdBy"] as IntNode).numberValue() as Int
////
////        return Item(id, itemName, User(userId, null))
//        jp.nextToken()
//
//    }
//}

class MeterReadingDeserializer @JvmOverloads constructor(vc: Class<*>? = null) : StdDeserializer<MeterReading>(vc) {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, context: DeserializationContext?): MeterReading? {
        //  val node: JsonNode = jp.readValueAsTree()
        //  return if (node.isEmpty) {
        //      null
        //   } else {
        jp.nextValue()

        val id = jp.readValueAs(UUID::class.java)
        jp.nextValue()
        val title = jp.readValueAs(String::class.java)
        jp.nextToken()
        jp.nextValue()
        jp.nextValue()
        jp.nextValue()
        val readingType = jp.readValueAs(MeterInfo::class.java)

        val now = jp.getText()
        //  .(UUID::class.java)
        // val id =        (node["id"] as Int).val() as Int
        //jp.close()

        return MeterReading(id = id, title = title, meterInfo = readingType)

        //  }
    }
}
