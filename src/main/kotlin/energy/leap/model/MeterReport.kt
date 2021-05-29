package energy.leap.model

import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class MeterReport(
    val id: UUID,
    val title: String,
    val meterInfo: MeterInfo,
    val priceSum: BigDecimal,
    val usageSum: BigDecimal,
    val hourlyData: Map<ZonedDateTime, HourData>
)

data class HourData(
    val usage: BigDecimal,
    val price: BigDecimal
)
