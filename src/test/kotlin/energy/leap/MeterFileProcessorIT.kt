package energy.leap

import org.junit.jupiter.api.Test
import java.io.File

internal class MeterFileProcessorIT {
    private val pathPrefix = "src/test/resources"

    private val subject = MeterFileProcessor()

    //@Disabled
    @Test
    fun `should process all meter files`() {
        File(pathPrefix).walk().forEach {
            if (!it.isDirectory) {
                subject.processFile(it)
            }

        }
    }

}
