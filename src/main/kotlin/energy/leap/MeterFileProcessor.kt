package energy.leap

import com.fasterxml.jackson.module.kotlin.readValue
import energy.leap.model.FlowDirection
import energy.leap.model.HourData
import energy.leap.model.MeterReading
import energy.leap.model.MeterReport
import mu.KotlinLogging
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.HOURS
import java.time.temporal.ChronoUnit.SECONDS

class MeterFileProcessor {
    private val xmlMapper = CustomXmlMapper()
    private val objectMapper = CustomObjectMapper()

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
        val meterName = report.title

        logger.debug { "Writing report for meter $meterName to file" }

        val timeStamp = LocalDateTime.now().truncatedTo(SECONDS)
        val fileName = "${meterName.replace(" ", "_")}-$timeStamp.json"
        val file = File("target/$fileName")

        objectMapper.getMapper().writeValue(file, report)

        logger.info { "Wrote report to ${file.absoluteFile}" }

    }

    private fun MeterReading.toReport(): MeterReport {
        val hourlyData: MutableMap<Long, BigDecimal> = mutableMapOf()

        intervalReadings.forEach {
            val duration = it.timePeriod.duration
            val startOfReading = it.timePeriod.start.toEpochSecond()
            val endOfReading = startOfReading + duration.toLong()
            val usagePerSecond =
                it.value.divide(duration, 10, RoundingMode.HALF_UP).applyFlow(meterInfo.flowDirection)

            for (second in startOfReading until endOfReading) {
                val startOfHour = second.toZonedDateTime().truncatedTo(HOURS).toEpochSecond()
                val usage = hourlyData[startOfHour] ?: BigDecimal.ZERO
                hourlyData[startOfHour] = usage.plus(usagePerSecond)
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
        ).also {
            logger.debug { "Generated meter report $it" }
        }
    }

    private fun MutableMap<Long, BigDecimal>.toReportFormat(unitPrice: BigDecimal) = map {
        val time = it.key.toZonedDateTime()
        val usage = it.value.roundTwoDecimals()
        val price = usage.multiply(unitPrice).roundTwoDecimals()

        time to HourData(usage = usage, price = price)
    }.toMap()

    private fun Long.toZonedDateTime() = ZonedDateTime.ofInstant(Instant.ofEpochSecond(this), ZoneOffset.UTC)
    private fun BigDecimal.roundTwoDecimals() = setScale(2, RoundingMode.HALF_UP)
    private fun BigDecimal.applyFlow(flow: FlowDirection) = if (flow == FlowDirection.UP) this.negate() else this
}
