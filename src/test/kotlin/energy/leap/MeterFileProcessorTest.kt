package energy.leap

import org.junit.jupiter.api.Test
import java.io.File

internal class MeterFileProcessorTest {
    private val pathPrefix = "src/test/resources/"

    private val subject = MeterFileProcessor()

    @Test
    fun `should process a ledger file`() {
        // given
        val file = File(pathPrefix + "ledger.xml")

        //when
        subject.processFile(file)

        //then
    }

    @Test
    fun `should process a simple file`() {
        // given
        val file = File(pathPrefix + "simple.xml")

        //when
        subject.processFile(file)

        //then
    }

    @Test
    fun `should process a test meter file`() {
        // given
        val file = File(pathPrefix + "meter_test.xml")

        //when
        subject.processFile(file)

        //then
    }
}
