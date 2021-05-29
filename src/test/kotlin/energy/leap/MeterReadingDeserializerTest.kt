package energy.leap

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualByComparingTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNotNull
import assertk.assertions.messageContains
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import energy.leap.model.FlowDirection
import energy.leap.model.MeterReading
import energy.leap.model.ReadingUnit
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.UUID

internal class MeterReadingDeserializerTest {
    private val xmlMapper = CustomXmlMapper()

    @Test
    internal fun `should convert meter xml to meter reading`() {
        val result = readValue<MeterReading>(
            """
                <feed>
                    <id>4b46b097-b80a-4e25-8852-44f88b9179ae</id>
                    <title type="text">Some Meter</title>
                    <entry>
                        <content>
                            <ReadingType>
                                <flowDirection>0</flowDirection>
                                <kWhPrice>0.123</kWhPrice>
                                <readingUnit>kWh</readingUnit>
                            </ReadingType>
                        </content>
                    </entry>
                    <entry>
                        <content>
                            <IntervalBlock>
                                <IntervalReading>
                                    <timePeriod>
                                        <duration>3600</duration>
                                        <start>1555484400</start>
                                    </timePeriod>
                                    <value>340000</value>
                                </IntervalReading>
                                <IntervalReading>
                                    <timePeriod>
                                        <duration>100.523</duration>
                                        <start>1555494350</start>
                                    </timePeriod>
                                    <value>49000.3322</value>
                                </IntervalReading>
                            </IntervalBlock>
                        </content>
                    </entry>
                </feed>
        """
        )

        assertThat(result.id).isEqualTo(UUID.fromString("4b46b097-b80a-4e25-8852-44f88b9179ae"))
        assertThat(result.title).isEqualTo("Some Meter")
        assertThat(result.meterInfo.flowDirection).isEqualTo(FlowDirection.DOWN)
        assertThat(result.meterInfo.unitPrice).isEqualByComparingTo("0.123")
        assertThat(result.meterInfo.readingUnit).isEqualTo(ReadingUnit.KWH)
        assertThat(result.intervalReadings.size).isEqualTo(2)
        assertThat(result.intervalReadings[0].timePeriod.duration).isEqualByComparingTo("3600")
        assertThat(result.intervalReadings[0].timePeriod.start).isSameInstant(ZonedDateTime.parse("2019-04-17T07:00:00Z"))
        assertThat(result.intervalReadings[0].value).isEqualByComparingTo("340000")
        assertThat(result.intervalReadings[1].timePeriod.duration).isEqualByComparingTo("100.523")
        assertThat(result.intervalReadings[1].timePeriod.start).isSameInstant(ZonedDateTime.parse("2019-04-17T09:45:50Z"))
        assertThat(result.intervalReadings[1].value).isEqualByComparingTo("49000.3322")
    }

    @Test
    internal fun `should accept meter xml with just one interval reading`() {
        val result = readValue<MeterReading>(
            """
                <feed>
                    <id>3b46b097-b80a-4e25-8852-44f88b9179ae</id>
                    <title type="text">Simple Meter</title>
                    <entry>
                        <content>
                            <ReadingType>
                                <flowDirection>0</flowDirection>
                                <kWhPrice>0.123</kWhPrice>
                                <readingUnit>kWh</readingUnit>
                            </ReadingType>
                        </content>
                    </entry>
                    <entry>
                        <content>
                            <IntervalBlock>
                                <IntervalReading>
                                    <timePeriod>
                                        <duration>3600</duration>
                                        <start>1555484400</start>
                                    </timePeriod>
                                    <value>340000</value>
                                </IntervalReading>
                            </IntervalBlock>
                        </content>
                    </entry>
                </feed>
        """
        )

        assertThat(result.id).isEqualTo(UUID.fromString("3b46b097-b80a-4e25-8852-44f88b9179ae"))
        assertThat(result.title).isEqualTo("Simple Meter")
        assertThat(result.meterInfo.flowDirection).isEqualTo(FlowDirection.DOWN)
        assertThat(result.meterInfo.unitPrice).isEqualByComparingTo("0.123")
        assertThat(result.meterInfo.readingUnit).isEqualTo(ReadingUnit.KWH)
        assertThat(result.intervalReadings.size).isEqualTo(1)
        assertThat(result.intervalReadings[0].timePeriod.duration).isEqualByComparingTo("3600")
        assertThat(result.intervalReadings[0].timePeriod.start).isSameInstant(ZonedDateTime.parse("2019-04-17T07:00:00Z"))
        assertThat(result.intervalReadings[0].value).isEqualByComparingTo("340000")
    }

    @Test
    internal fun `should reject reading without intervals`() {
        assertThat {
            readValue<MeterReading>(
                """
                <feed>
                    <id>3b46b097-b80a-4e25-8852-44f88b9179ae</id>
                    <title type="text">Simple Meter</title>
                    <entry>
                        <content>
                            <ReadingType>
                                <flowDirection>1</flowDirection>
                                <kWhPrice>0.123</kWhPrice>
                                <readingUnit>kWh</readingUnit>
                            </ReadingType>
                        </content>
                    </entry>
                    <entry>
                        <content>
                            <IntervalBlock>
                            </IntervalBlock>
                        </content>
                    </entry>
                </feed>
        """
            )
        }.isFailure().all {
            hasClass(MismatchedInputException::class.java)
            messageContains("""Cannot deserialize value of type `energy.leap.model.IntervalReading` from [Unavailable value] (token `JsonToken.NOT_AVAILABLE`)""")
        }
    }

    @Test
    internal fun `should reject with missing required field`() {
        assertThat {
            readValue<MeterReading>(
                """
                <feed>
                    <title type="text">Simple Meter</title>
                    <entry>
                        <content>
                            <ReadingType>
                                <flowDirection>1</flowDirection>
                                <kWhPrice>0.123</kWhPrice>
                                <readingUnit>kWh</readingUnit>
                            </ReadingType>
                        </content>
                    </entry>
                    <entry>
                        <content>
                            <IntervalBlock>
                            </IntervalBlock>
                        </content>
                    </entry>
                </feed>
        """
            )
        }.isFailure().all {
            hasClass(MismatchedInputException::class.java)
            messageContains("""Cannot deserialize value of type `java.util.UUID` from [Unavailable value] (token `JsonToken.NOT_AVAILABLE`)""")
        }
    }

    @Test
    internal fun `should reject with unknown enum`() {
        assertThat {
            readValue<MeterReading>(
                """
                <feed>
                    <id>3b46b097-b80a-4e25-8852-44f88b9179ae</id>
                    <title type="text">Simple Meter</title>
                    <entry>
                        <content>
                            <ReadingType>
                                <flowDirection>?</flowDirection>
                                <kWhPrice>0.123</kWhPrice>
                                <readingUnit>kWh</readingUnit>
                            </ReadingType>
                        </content>
                    </entry>
                    <entry>
                        <content>
                            <IntervalBlock>
                                <IntervalReading>
                                    <timePeriod>
                                        <duration>100.523</duration>
                                        <start>1555494350</start>
                                    </timePeriod>
                                    <value>49000.3322</value>
                                </IntervalReading>
                            </IntervalBlock>
                        </content>
                    </entry>
                </feed>
        """
            )
        }.isFailure().all {
            hasClass(InvalidFormatException::class.java)
            messageContains("""Cannot deserialize value of type `energy.leap.model.FlowDirection` from String "?": not one of the values accepted for Enum""")
        }
    }

    fun Assert<ZonedDateTime?>.isSameInstant(expected: ZonedDateTime) = given {
        assertThat(it).isNotNull()
        assertThat(it!!.withZoneSameInstant(expected.zone)).isEqualTo(expected)
    }

    private inline fun <reified T> readValue(str: String) = xmlMapper.getMapper().readValue<T>(str.trimIndent().trim())

}
