package energy.leap

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import energy.leap.model.MeterReading

class CustomXmlMapper {
    private val xmlMapper: XmlMapper

    init {
        val deserializerModule = SimpleModule()
            .addDeserializer(MeterReading::class.java, MeterReadingDeserializer())

        xmlMapper = XmlMapper
            .builder()
            .addModule(deserializerModule)
            .build()
    }

    fun getMapper() = xmlMapper
}
