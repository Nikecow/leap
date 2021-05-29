package energy.leap

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import energy.leap.model.MeterReading

class CustomXmlMapper {
    private val xmlMapper: XmlMapper

    init {
        val deserializerModule = SimpleModule()
            .addDeserializer(MeterReading::class.java, MeterReadingDeserializer())

        xmlMapper = XmlMapper
            .builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .addModule(KotlinModule())
            .addModule(deserializerModule)
            .build()
    }

    fun getMapper() = xmlMapper
}
