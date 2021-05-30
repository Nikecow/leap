package energy.leap

import assertk.assertThat
import assertk.assertions.isEqualByComparingTo
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.readValue
import energy.leap.model.FlowDirection
import energy.leap.model.MeterReport
import energy.leap.model.ReadingUnit
import mu.KotlinLogging
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.time.ZonedDateTime
import java.util.UUID

internal class MeterFileProcessorIT {
    private val pathPrefix = "src/test/resources/"

    private val customObjectMapper = CustomObjectMapper()
    private val subject = MeterFileProcessor(customObjectMapper)

    private val logger = KotlinLogging.logger { }

    @Disabled
    @Test
    internal fun `should process all meter files`() {
        File(pathPrefix).walk().forEach {
            if (!it.isDirectory) {
                try {
                    subject.processFile(it)
                } catch (ex: JsonParseException) {
                    logger.warn { "Skipping file ${it.name}" }
                }
            }
        }
    }

    @Test
    internal fun `should generate a report for a meter file`() {
        // given
        val file = File(pathPrefix + "meter1.xml")

        // when
        val report = subject.processFile(file)
        val actual = customObjectMapper.getMapper().readValue<MeterReport>(report)

        // then
        assertThat(actual.id).isEqualTo(UUID.fromString("1a46b097-b80a-4e25-8852-44f88b9179ae"))
        assertThat(actual.title).isEqualTo("Green Button Usage Feed")
        assertThat(actual.meterInfo.flowDirection).isEqualTo(FlowDirection.UP)
        assertThat(actual.meterInfo.unitPrice).isEqualByComparingTo("0.07")
        assertThat(actual.meterInfo.readingUnit).isEqualTo(ReadingUnit.KWH)
        assertThat(actual.priceSum).isEqualByComparingTo("45500.00")
        assertThat(actual.usageSum).isEqualByComparingTo("649999.99999975500000000000")
        assertThat(actual.hourlyData[ZonedDateTime.parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("340000.00")
        assertThat(actual.hourlyData[ZonedDateTime.parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("23800.00")
        assertThat(actual.hourlyData[ZonedDateTime.parse("2019-04-17T08:00:00Z")]!!.usage).isEqualByComparingTo("260000.000")
        assertThat(actual.hourlyData[ZonedDateTime.parse("2019-04-17T08:00:00Z")]!!.price).isEqualByComparingTo("18200.00")
        assertThat(actual.hourlyData[ZonedDateTime.parse("2019-04-17T09:00:00Z")]!!.usage).isEqualByComparingTo("50000.00")
        assertThat(actual.hourlyData[ZonedDateTime.parse("2019-04-17T09:00:00Z")]!!.price).isEqualByComparingTo("3500.00")
    }
}
