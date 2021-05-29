package energy.leap

import assertk.assertThat
import assertk.assertions.isEqualByComparingTo
import assertk.assertions.isEqualTo
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
    fun `should process a meter file in kWh`() {
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
            assertThat(firstValue.usageSum).isEqualByComparingTo("649999.99999975500000000000")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("340000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("23800.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.usage).isEqualByComparingTo("260000.000")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.price).isEqualByComparingTo("18200.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.usage).isEqualByComparingTo("50000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.price).isEqualByComparingTo("3500.00")
        }
    }

    @Test
    fun `should process a meter file with Wh`() {
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
            assertThat(firstValue.usageSum).isEqualByComparingTo("510.00000009750000000000")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("200.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("14.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.usage).isEqualByComparingTo("260.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.price).isEqualByComparingTo("18.20")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.usage).isEqualByComparingTo("50.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.price).isEqualByComparingTo("3.50")
        }
    }

    @Test
    fun `should process a leeching meter`() {
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
            assertThat(firstValue.usageSum).isEqualByComparingTo("339999.9999998400")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("340000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("-23800.00")
        }
    }

    @Test
    fun `should process an unordered meter file`() {
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
            assertThat(firstValue.usageSum).isEqualByComparingTo("649999.99999975500000000000")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.usage).isEqualByComparingTo("50000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.price).isEqualByComparingTo("3500.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.usage).isEqualByComparingTo("260000.000")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.price).isEqualByComparingTo("18200.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("340000.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("23800.00")
        }
    }

    @Test
    fun `should process a disjointed meter file`() {
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
            assertThat(firstValue.usageSum).isEqualByComparingTo("225.00000015750000000000")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("200.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("14.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.usage).isEqualByComparingTo("10.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.price).isEqualByComparingTo("0.70")
            assertThat(firstValue.hourlyData[parse("2019-04-17T11:00:00Z")]!!.usage).isEqualByComparingTo("15.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T11:00:00Z")]!!.price).isEqualByComparingTo("1.05")
        }
    }

    @Test
    fun `should process an overlapping meter file`() {
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
            assertThat(firstValue.usageSum).isEqualByComparingTo("210.00000015000000000000")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("210.00")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("21.00")
        }
    }

    @Test
    fun `should process a meter with lots of decimal values`() {
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
            assertThat(firstValue.usageSum).isEqualByComparingTo("150.34394425901138888980")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.usage).isEqualByComparingTo("100.12")
            assertThat(firstValue.hourlyData[parse("2019-04-17T07:00:00Z")]!!.price).isEqualByComparingTo("0.12")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.usage).isEqualByComparingTo("50.02")
            assertThat(firstValue.hourlyData[parse("2019-04-17T08:00:00Z")]!!.price).isEqualByComparingTo("0.06")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.usage).isEqualByComparingTo("0.20")
            assertThat(firstValue.hourlyData[parse("2019-04-17T09:00:00Z")]!!.price).isEqualByComparingTo("0.00")
        }
    }
}
