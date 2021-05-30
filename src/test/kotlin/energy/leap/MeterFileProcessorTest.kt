package energy.leap

import assertk.assertThat
import assertk.assertions.isEqualByComparingTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.messageContains
import com.fasterxml.jackson.core.JsonParseException
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import energy.leap.model.FlowDirection
import energy.leap.model.MeterReport
import energy.leap.model.ReadingUnit
import org.junit.jupiter.api.Test
import java.io.File
import java.time.ZonedDateTime.parse
import java.util.UUID

internal class MeterFileProcessorTest {
    private val pathPrefix = "src/test/resources/"
    private val customObjectMapper: CustomObjectMapper = mock {}

    private val subject = MeterFileProcessor(customObjectMapper)

    @Test
    internal fun `should process meter file with unit type in kWh`() {
        // given
        val file = File(pathPrefix + "meter1.xml")

        // when
        subject.processFile(file)

        // then
        argumentCaptor<MeterReport>().apply {
            verify(customObjectMapper).writeToFile(
                eq(File("target/Green_Button_Usage_Feed_1a46b097-b80a-4e25-8852-44f88b9179ae.json")),
                capture()
            )

            assertThat(firstValue.id).isEqualTo(UUID.fromString("1a46b097-b80a-4e25-8852-44f88b9179ae"))
            assertThat(firstValue.title).isEqualTo("Green Button Usage Feed")
            assertThat(firstValue.meterInfo.flowDirection).isEqualTo(FlowDirection.UP)
            assertThat(firstValue.meterInfo.unitPrice).isEqualByComparingTo("0.07")
            assertThat(firstValue.meterInfo.readingUnit).isEqualTo(ReadingUnit.KWH)
            assertThat(firstValue.priceSum).isEqualByComparingTo("45500.00")
            assertThat(firstValue.usageSum).isEqualByComparingTo("650000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("340000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("23800.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.usage).isEqualByComparingTo("260000.000")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.price).isEqualByComparingTo("18200.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.usage).isEqualByComparingTo("50000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.price).isEqualByComparingTo("3500.00")
        }
    }

    @Test
    internal fun `should process meter file with unit type in Wh`() {
        // given
        val file = File(pathPrefix + "meter2.xml")

        // when
        subject.processFile(file)

        // then
        argumentCaptor<MeterReport>().apply {
            verify(customObjectMapper).writeToFile(
                eq(File("target/Green_Button_Usage_Feed_9346bfb3-20aa-3412-ffab-44f88b917999.json")),
                capture()
            )

            assertThat(firstValue.id).isEqualTo(UUID.fromString("9346bfb3-20aa-3412-ffab-44f88b917999"))
            assertThat(firstValue.title).isEqualTo("Green Button Usage Feed")
            assertThat(firstValue.meterInfo.flowDirection).isEqualTo(FlowDirection.UP)
            assertThat(firstValue.meterInfo.unitPrice).isEqualByComparingTo("0.07")
            assertThat(firstValue.meterInfo.readingUnit).isEqualTo(ReadingUnit.WH)
            assertThat(firstValue.priceSum).isEqualByComparingTo("35.70")
            assertThat(firstValue.usageSum).isEqualByComparingTo("510.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("200.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("14.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.usage).isEqualByComparingTo("260.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.price).isEqualByComparingTo("18.20")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.usage).isEqualByComparingTo("50.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.price).isEqualByComparingTo("3.50")
        }
    }

    @Test
    internal fun `should process meter with leeching flow direction`() {
        // given
        val file = File(pathPrefix + "leeching-meter.xml")

        // when
        subject.processFile(file)

        // then
        argumentCaptor<MeterReport>().apply {
            verify(customObjectMapper).writeToFile(
                eq(File("target/Leeching_Meter_3346bfb3-20aa-3412-ffab-44f88b9179cc.json")),
                capture()
            )

            assertThat(firstValue.id).isEqualTo(UUID.fromString("3346bfb3-20aa-3412-ffab-44f88b9179cc"))
            assertThat(firstValue.title).isEqualTo("Leeching Meter")
            assertThat(firstValue.meterInfo.flowDirection).isEqualTo(FlowDirection.DOWN)
            assertThat(firstValue.meterInfo.unitPrice).isEqualByComparingTo("0.07")
            assertThat(firstValue.meterInfo.readingUnit).isEqualTo(ReadingUnit.KWH)
            assertThat(firstValue.priceSum).isEqualByComparingTo("-23800.00")
            assertThat(firstValue.usageSum).isEqualByComparingTo("340000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("340000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("-23800.00")
        }
    }

    @Test
    internal fun `should process meter file with unordered readings`() {
        // given
        val file = File(pathPrefix + "unordered-meter.xml")

        // when
        subject.processFile(file)

        // then
        argumentCaptor<MeterReport>().apply {
            verify(customObjectMapper).writeToFile(
                eq(File("target/Unordered_Meter_2c46b097-b80a-4e25-8852-44f88b9179ee.json")),
                capture()
            )

            assertThat(firstValue.id).isEqualTo(UUID.fromString("2c46b097-b80a-4e25-8852-44f88b9179ee"))
            assertThat(firstValue.title).isEqualTo("Unordered Meter")
            assertThat(firstValue.meterInfo.flowDirection).isEqualTo(FlowDirection.UP)
            assertThat(firstValue.meterInfo.unitPrice).isEqualByComparingTo("0.07")
            assertThat(firstValue.meterInfo.readingUnit).isEqualTo(ReadingUnit.KWH)
            assertThat(firstValue.priceSum).isEqualByComparingTo("45500.00")
            assertThat(firstValue.usageSum).isEqualByComparingTo("650000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.usage).isEqualByComparingTo("50000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.price).isEqualByComparingTo("3500.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.usage).isEqualByComparingTo("260000.000")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.price).isEqualByComparingTo("18200.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("340000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("23800.00")
        }
    }

    @Test
    internal fun `should process a meter file with disjointed readings`() {
        // given
        val file = File(pathPrefix + "disjointed-meter.xml")

        // when
        subject.processFile(file)

        // then
        argumentCaptor<MeterReport>().apply {
            verify(customObjectMapper).writeToFile(
                eq(File("target/Disjointed_Meter_2646bfb3-20aa-3412-ffab-44f88b917911.json")),
                capture()
            )

            assertThat(firstValue.id).isEqualTo(UUID.fromString("2646bfb3-20aa-3412-ffab-44f88b917911"))
            assertThat(firstValue.title).isEqualTo("Disjointed Meter")
            assertThat(firstValue.meterInfo.flowDirection).isEqualTo(FlowDirection.UP)
            assertThat(firstValue.meterInfo.unitPrice).isEqualByComparingTo("0.07")
            assertThat(firstValue.meterInfo.readingUnit).isEqualTo(ReadingUnit.WH)
            assertThat(firstValue.priceSum).isEqualByComparingTo("15.75")
            assertThat(firstValue.usageSum).isEqualByComparingTo("225.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("200.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("14.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.usage).isEqualByComparingTo("10.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.price).isEqualByComparingTo("0.70")
            assertThat(firstValue.hourlyData[parse("2019-04-17T11:00:00Z")]!!.usage).isEqualByComparingTo("15.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T11:00:00Z")]!!.price).isEqualByComparingTo("1.05")
        }
    }

    @Test
    internal fun `should process meter file with overlapping readings`() {
        // given
        val file = File(pathPrefix + "overlapping-meter.xml")

        // when
        subject.processFile(file)

        // then
        argumentCaptor<MeterReport>().apply {
            verify(customObjectMapper).writeToFile(
                eq(File("target/Overlapping_Meter_4446bfb3-20aa-3412-ffab-44f88b917955.json")),
                capture()
            )

            assertThat(firstValue.id).isEqualTo(UUID.fromString("4446bfb3-20aa-3412-ffab-44f88b917955"))
            assertThat(firstValue.title).isEqualTo("Overlapping Meter")
            assertThat(firstValue.meterInfo.flowDirection).isEqualTo(FlowDirection.UP)
            assertThat(firstValue.meterInfo.unitPrice).isEqualByComparingTo("0.10")
            assertThat(firstValue.meterInfo.readingUnit).isEqualTo(ReadingUnit.WH)
            assertThat(firstValue.priceSum).isEqualByComparingTo("21.00")
            assertThat(firstValue.usageSum).isEqualByComparingTo("210.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("210.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("21.00")
        }
    }

    @Test
    internal fun `should process a meter with lots of decimal values`() {
        // given
        val file = File(pathPrefix + "decimal-meter.xml")

        // when
        subject.processFile(file)

        // then
        argumentCaptor<MeterReport>().apply {
            verify(customObjectMapper).writeToFile(
                eq(File("target/Decimal_Meter_9846b097-b80a-4e25-8852-44f88b9179ae.json")),
                capture()
            )

            assertThat(firstValue.id).isEqualTo(UUID.fromString("9846b097-b80a-4e25-8852-44f88b9179ae"))
            assertThat(firstValue.title).isEqualTo("Decimal Meter")
            assertThat(firstValue.meterInfo.flowDirection).isEqualTo(FlowDirection.UP)
            assertThat(firstValue.meterInfo.unitPrice).isEqualByComparingTo("0.00123")
            assertThat(firstValue.meterInfo.readingUnit).isEqualTo(ReadingUnit.KWH)
            assertThat(firstValue.priceSum).isEqualByComparingTo("0.18")
            assertThat(firstValue.usageSum).isEqualByComparingTo("150.34")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("100.12")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("0.12")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.usage).isEqualByComparingTo("50.02")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.price).isEqualByComparingTo("0.06")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.usage).isEqualByComparingTo("0.20")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.price).isEqualByComparingTo("0.00")
        }
    }

    @Test
    internal fun `should process a meter with readings spanning multiple days`() {
        // given
        val file = File(pathPrefix + "multiple-days-meter.xml")

        // when
        subject.processFile(file)

        // then
        argumentCaptor<MeterReport>().apply {
            verify(customObjectMapper).writeToFile(
                eq(File("target/Multiple_Days_Meter_2216bfb3-20aa-3412-ffab-44f88b917988.json")),
                capture()
            )

            assertThat(firstValue.id).isEqualTo(UUID.fromString("2216bfb3-20aa-3412-ffab-44f88b917988"))
            assertThat(firstValue.title).isEqualTo("Multiple Days Meter")
            assertThat(firstValue.meterInfo.flowDirection).isEqualTo(FlowDirection.UP)
            assertThat(firstValue.meterInfo.unitPrice).isEqualByComparingTo("0.10")
            assertThat(firstValue.meterInfo.readingUnit).isEqualTo(ReadingUnit.WH)
            assertThat(firstValue.priceSum).isEqualByComparingTo("135.00")
            assertThat(firstValue.usageSum).isEqualByComparingTo("1350.00")
            assertThat(firstValue.hourlyData[parse("2021-05-30T23:00:00Z")]!!.usage).isEqualByComparingTo("50.00")
            assertThat(firstValue.hourlyData[parse("2021-05-30T23:00:00Z")]!!.price).isEqualByComparingTo("5.00")
            assertThat(firstValue.hourlyData[parse("2021-05-31T00:00:00Z")]!!.usage).isEqualByComparingTo("300.00")
            assertThat(firstValue.hourlyData[parse("2021-05-31T00:00:00Z")]!!.price).isEqualByComparingTo("30.00")
            assertThat(firstValue.hourlyData[parse("2021-06-01T00:00:00Z")]!!.usage).isEqualByComparingTo("1000.00")
            assertThat(firstValue.hourlyData[parse("2021-06-01T00:00:00Z")]!!.price).isEqualByComparingTo("100.00")
        }
    }

    @Test
    internal fun `should reject corrupted file`() {
        assertThat {
            val file = File(pathPrefix + "corrupted-meter.xml")

            subject.processFile(file)
        }.isFailure()
            .isInstanceOf(JsonParseException::class).given {
                assertThat(it.cause).isNotNull().messageContains("Invalid UTF-8 middle byte 0xe (at char #0, byte #-1)")
            }
    }
}
