package energy.leap

import com.fasterxml.jackson.databind.MappingIterator
import mu.KotlinLogging
import java.io.File

class MeterFileProcessor {

    private val logger = KotlinLogging.logger { }
    private val xmlMapper = CustomXmlMapper()

    fun processFile(file: File) {

        logger.info { "Parsing file ${file.name}" }

//
        file.inputStream().bufferedReader().use {
//            parseAs<>(it)
            //val s = f.createXMLStreamReader( it)
            val recordIterator: MappingIterator<SimpleValue> =
                xmlMapper.getMapper().readerFor(SimpleValue::class.java).readValues(it)

            while (recordIterator.hasNext()) {
                val next = recordIterator.next()
                logger.info { next }

            }
        }

        //  val readings = xmlMapper.getMapper().readValue<IntervalReading>(file)
        //    val readings = xmlMapper.getMapper().readValue<LedgerActivities>(file)

        //  logger.info { "Parsed file, retrieved ${readings} entries" }
        //  logger.info { "Parsed file, retrieved $readings" }

    }

//    internal inline fun <reified T : Any> parseAs(input: BufferedReader) {
//        return xmlMapper.getMapper().readValue(input)
//    }
}

//fun main() {
//    MeterFileProcessor().processFile()
//}
