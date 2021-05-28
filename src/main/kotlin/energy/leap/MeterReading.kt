package energy.leap

import com.fasterxml.jackson.annotation.JsonValue
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class MeterReading(
    val id: UUID,
    val title: String,
    val meterInfo: MeterInfo,
    val intervalReadings: List<IntervalReading>
)

data class MeterInfo(
    val flowDirection: FlowDirection,
    val unitPrice: BigDecimal,
    val readingUnit: ReadingUnit
)

data class IntervalReading(
    val timePeriod: TimePeriod,
    val value: BigDecimal
)

data class TimePeriod(
    val duration: BigDecimal,
    val start: ZonedDateTime
)

enum class FlowDirection(private val value: Int) {
    DOWN(0), UP(1);

    @JsonValue
    open fun toValue(): Int {
        return value
    }
}

enum class ReadingUnit {
    KWH, MWH, GWH // add assumption
}
