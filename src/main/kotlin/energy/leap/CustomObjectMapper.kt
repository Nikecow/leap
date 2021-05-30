package energy.leap

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

class CustomObjectMapper {
    private val objectMapper: ObjectMapper

    init {
        objectMapper = ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
    }

    fun getMapper() = objectMapper

    fun writeToFile(file: File, any: Any) = objectMapper.writeValue(file, any)
}
