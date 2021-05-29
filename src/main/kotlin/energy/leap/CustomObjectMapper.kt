package energy.leap

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class CustomObjectMapper {
    private val objectMapper: ObjectMapper

    init {
        objectMapper = ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
    }

    fun getMapper() = objectMapper
}
