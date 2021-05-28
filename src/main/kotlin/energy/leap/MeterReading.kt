package energy.leap

import java.util.UUID
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "feed")
data class MeterReading(
    val id: UUID,
    val title: String,
    val meterInfo: MeterInfo
)

data class MeterInfo(
    val flowDirection: Int, // add assumption, 0 or 1 bit, 0 means dat het er van af gaat, boolean
    val kWhPrice: Float, // add assumption, precision, BigDecimal
    val readingUnit: ReadingUnit
)

//data class IntervalReading

enum class ReadingUnit {
    KWH, MWH, GWH // add assumption
}
