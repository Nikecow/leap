package energy.leap

import com.fasterxml.jackson.module.kotlin.readValue
import energy.leap.model.HourData
import energy.leap.model.MeterReading
import energy.leap.model.MeterReport
import mu.KotlinLogging
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

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
            val duration = it.timePeriod.duration
            val startOfReading = it.timePeriod.start.toEpochSecond()
            val endOfReading = startOfReading + duration.toLong()
            val usagePerSecond = it.value.divide(duration, 10, RoundingMode.HALF_UP)

            var epochSecond = startOfReading

            while (epochSecond < endOfReading) {
                val startOfHour = epochSecond.toZonedDateTime().truncatedTo(ChronoUnit.HOURS).toEpochSecond()
                val usage = hourlyData[startOfHour] ?: BigDecimal.ZERO
                hourlyData[startOfHour] = usage.plus(usagePerSecond)

                epochSecond += 1
            }
        }
        val totalUsage = hourlyData.values.reduce(BigDecimal::add)
        val totalPrice = totalUsage.multiply(meterInfo.unitPrice).roundTwoDecimals()

        return MeterReport(
            id = id,
            title = title,
            meterInfo = meterInfo,
            totalPrice = totalPrice,
            totalUsage = totalUsage,
            hourlyData = hourlyData.toReportFormat(meterInfo.unitPrice)
        ).also { logger.info { "Generated meter report $it" } }

    }

    private fun MutableMap<Long, BigDecimal>.toReportFormat(unitPrice: BigDecimal) = map {
        val time = it.key.toZonedDateTime()
        val usage = it.value.roundTwoDecimals()
        val price = usage.multiply(unitPrice).roundTwoDecimals()

        time to HourData(usage = usage, price = price)
    }.toMap()

    private fun Long.toZonedDateTime() = ZonedDateTime.ofInstant(Instant.ofEpochSecond(this), ZoneOffset.UTC)
    private fun BigDecimal.roundTwoDecimals() = setScale(2, RoundingMode.HALF_UP)

}
