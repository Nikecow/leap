package energy.leap

import com.fasterxml.jackson.module.kotlin.readValue
import energy.leap.model.FlowDirection
import energy.leap.model.HourData
import energy.leap.model.MeterReading
import energy.leap.model.MeterReport
import mu.KotlinLogging
import java.io.File
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.math.RoundingMode.HALF_UP
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.HOURS

const val SECONDS_IN_HOUR = 3600L

class MeterFileProcessor(private val objectMapper: CustomObjectMapper) {
    private val xmlMapper = CustomXmlMapper()

    private val logger = KotlinLogging.logger { }

    fun processFile(file: File): File {
        logger.info { "Processing file ${file.name}" }

        val reading = xmlMapper.getMapper().readValue<MeterReading>(file)

        return generateReport(reading.toReport())
    }

    private fun generateReport(report: MeterReport): File {
        val meterName = report.title
        val id = report.id

        logger.debug { "Writing report for meter $meterName with id $id to file" }

        val fileName = "${meterName.replace(" ", "_")}_$id.json"
        val file = File("target/$fileName")

        objectMapper.writeToFile(file, report)

        return file.also { logger.info { "Wrote report to ${it.absoluteFile}" } }
    }

    private fun MeterReading.toReport(): MeterReport {
        val hourMap: MutableMap<Long, BigDecimal> = mutableMapOf()

        intervalReadings.forEach {
            val duration = it.timePeriod.duration
            val startOfReading = it.timePeriod.start.toEpochSecond()
            val endOfReading = startOfReading + duration.toLong()
            val hourUsage = duration.divideWithScale(SECONDS_IN_HOUR.toBigDecimal())
            val usagePerSecond = it.value.divideWithScale(duration).multiply(hourUsage)

            for (second in startOfReading until endOfReading) {
                val startOfHour = second.toZonedDateTime().truncatedTo(HOURS).toEpochSecond()
                val usage = hourMap[startOfHour] ?: ZERO
                hourMap[startOfHour] = usage.plus(usagePerSecond)
            }
        }

        val usageSum = hourMap.values.reduce(BigDecimal::add).roundTwoDecimals()
        val priceSum = usageSum.multiply(meterInfo.unitPrice).applyFlow(meterInfo.flowDirection).roundTwoDecimals()
        val hourlyData = hourMap.toReportFormat(meterInfo.unitPrice.applyFlow(meterInfo.flowDirection))

        return MeterReport(
            id = id,
            title = title,
            meterInfo = meterInfo,
            priceSum = priceSum,
            usageSum = usageSum,
            hourlyData = hourlyData
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
    private fun BigDecimal.roundTwoDecimals() = setScale(2, HALF_UP)
    private fun BigDecimal.applyFlow(flow: FlowDirection) = if (flow == FlowDirection.UP) this else this.negate()
    private fun BigDecimal.divideWithScale(bd: BigDecimal) = divide(bd, 10, HALF_UP)
}
