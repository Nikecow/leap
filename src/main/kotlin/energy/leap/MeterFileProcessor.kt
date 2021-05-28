package energy.leap

import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import java.io.File

class MeterFileProcessor {

    private val logger = KotlinLogging.logger { }
    private val xmlMapper = CustomXmlMapper()

    fun processFile(file: File) {
        logger.info { "Parsing file ${file.name}" }

        val reading = xmlMapper.getMapper().readValue<MeterReading>(file)

        logger.info { "Parsed file, retrieved reading: $reading" }
    }
}
