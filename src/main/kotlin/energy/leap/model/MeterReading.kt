package energy.leap.model

import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class MeterReading(
    val id: UUID,
    val title: String,
    val meterInfo: MeterInfo,
    val intervalReadings: List<IntervalReading>
)

data class IntervalReading(
    val timePeriod: TimePeriod,
    val value: BigDecimal
)

data class TimePeriod(
    val duration: BigDecimal,
    val start: ZonedDateTime
)
