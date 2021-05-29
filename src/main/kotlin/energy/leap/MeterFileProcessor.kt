package energy.leap

import com.fasterxml.jackson.module.kotlin.readValue
import energy.leap.model.HourData
import energy.leap.model.MeterReading
import energy.leap.model.MeterReport
import mu.KotlinLogging
import java.io.File
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

const val HOUR_DURATION_IN_SECONDS = 3600L

class MeterFileProcessor {
    private val xmlMapper = CustomXmlMapper()
    private val logger = KotlinLogging.logger { }

    fun processFile(file: File) {
        logger.info { "Processing file ${file.name}" }

        val reading = readFile(file)

        generateReport(reading.toReport())

    }

    private fun readFile(file: File) =
        xmlMapper.getMapper().readValue<MeterReading>(file).also {
            logger.debug { "Parsed file, retrieved reading: $it" }
        }

    private fun generateReport(report: MeterReport) {

    }

    private fun MeterReading.toReport(): MeterReport {
        val hourlyData: MutableMap<Long, BigDecimal> = mutableMapOf()

        intervalReadings.forEach {
            val start = it.timePeriod.start
            val duration = it.timePeriod.duration
            val startOfReading = start.toEpochSecond()
            val startOfHour = start.truncatedTo(ChronoUnit.HOURS).toEpochSecond()
            val endOfReading = startOfReading + duration.toLong()
            val usagePerSecond = it.value / duration

            var epochSecond = startOfReading

            while (epochSecond < endOfReading) {
                val hour =
                    epochSecond + ((epochSecond - startOfHour) % HOUR_DURATION_IN_SECONDS) * HOUR_DURATION_IN_SECONDS
                val usage = hourlyData[hour] ?: BigDecimal.ZERO
                hourlyData[hour] = usage + usagePerSecond

                epochSecond += 1
            }
        }
        val totalUsage = hourlyData.values.reduce(BigDecimal::add)
        val totalPrice = totalUsage * meterInfo.unitPrice

        return MeterReport(
            id = id,
            title = title,
            meterInfo = meterInfo,
            totalPrice = totalPrice,
            totalUsage = totalUsage,
            hourlyData = hourlyData.toReportFormat(meterInfo.unitPrice)
        ).also { logger.info { "Generated meter report $it " } }

    }

    private fun MutableMap<Long, BigDecimal>.toReportFormat(unitPrice: BigDecimal) = map {
        val localDateTime = LocalDateTime.ofEpochSecond(it.key, 0, ZoneOffset.UTC)
        val usage = it.value
        val price = usage * unitPrice

        localDateTime to HourData(usage = usage, price = price)
    }.toMap()
}
