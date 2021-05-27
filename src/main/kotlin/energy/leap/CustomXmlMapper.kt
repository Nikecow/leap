package energy.leap

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper

class CustomXmlMapper {
    private val xmlMapper: XmlMapper

    init {
//        xmlMapper = XmlMapper.builder()
//            .defaultUseWrapper(false)
//            .enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
//           .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//            .addModule(JacksonXmlModule())
//            .build()

        val module = JacksonXmlModule()
        module.setDefaultUseWrapper(true)
        xmlMapper = XmlMapper(module)
        xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        xmlMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE)

    }

    fun getMapper() = xmlMapper
}
