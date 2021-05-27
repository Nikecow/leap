package energy.leap

import com.fasterxml.jackson.annotation.JacksonAnnotation
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("IntervalBlock")
data class IntervalReadings(

    @set:JsonProperty("IntervalReading")
    var readings: List<IntervalReading> = ArrayList()
)

@JsonRootName("IntervalReading")
data class IntervalReading(

    @set:JsonProperty("value")
    var value: Long? = null,

    )

@Retention
@JacksonAnnotation
annotation class SkipWrapperObject(val value: String)

data class SimpleValues(

    var values: List<SimpleValue> = emptyList()

)

data class SimpleValue(

    var value: Long? = null,

    )
