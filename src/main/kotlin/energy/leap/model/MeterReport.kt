package energy.leap.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class MeterReport(
    val id: UUID,
    val title: String,
    val meterInfo: MeterInfo,
    val totalPrice: BigDecimal,
    val totalUsage: BigDecimal,
    val hourlyData: Map<LocalDateTime, HourData>
)

data class HourData(
    val usage: BigDecimal,
    val price: BigDecimal
)
