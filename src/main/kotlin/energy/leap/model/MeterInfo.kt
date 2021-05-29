package energy.leap.model

import com.fasterxml.jackson.annotation.JsonValue
import java.math.BigDecimal

data class MeterInfo(
    val flowDirection: FlowDirection,
    val unitPrice: BigDecimal,
    val readingUnit: ReadingUnit
)

enum class FlowDirection(private val value: Int) {
    DOWN(0), UP(1);

    @JsonValue
    open fun toValue(): Int {
        return value
    }
}

enum class ReadingUnit {
    KWH, MWH, GWH
}
