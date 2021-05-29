package energy.leap

import assertk.assertThat
import assertk.assertions.isSuccess
import org.junit.jupiter.api.Test
import java.io.File

internal class MeterFileProcessorTest {
    private val pathPrefix = "src/test/resources/"
    private val subject = MeterFileProcessor()

    @Test
    fun `should process a meter file`() {
        // given
        val file = File(pathPrefix + "meter1.xml")

        // when, then
        assertThat {
            subject.processFile(file)
        }.isSuccess()
    }
}
