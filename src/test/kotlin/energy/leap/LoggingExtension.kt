package energy.leap

import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.AppenderBase
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

class LoggingExtension(subjectClass: KClass<*>) : BeforeEachCallback, AfterAllCallback, ParameterResolver {
    private val subjectLogger: Logger = LoggerFactory.getLogger(subjectClass.java) as Logger
    val events: MutableList<LoggingEvent> = CopyOnWriteArrayList()
    private val appender: Appender<ILoggingEvent> = TestAppender(events) as Appender<ILoggingEvent>

    companion object {
        fun of(`class`: KClass<*>): LoggingExtension {
            return LoggingExtension(`class`)
        }
    }

    override fun beforeEach(context: ExtensionContext?) {
        if (!appender.isStarted) {
            appender.start()
            subjectLogger.addAppender(appender)
        }
        events.clear()
    }

    override fun afterAll(context: ExtensionContext?) {
        appender.stop()
        subjectLogger.detachAppender(appender)
    }

    override fun supportsParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Boolean {
        return parameterContext?.parameter?.type == List::class.java || parameterContext?.parameter?.parameterizedType == LoggingEvent::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Any {
        return events
    }
}

class TestAppender(private val events: MutableList<LoggingEvent>) : AppenderBase<LoggingEvent>() {
    override fun append(e: LoggingEvent) {
        events.add(e)
    }
}

fun Assert<LoggingEvent>.level() = prop("level", LoggingEvent::getLevel)

fun Assert<LoggingEvent>.message() = prop("message", LoggingEvent::getMessage)

fun Assert<LoggingEvent>.hasArgument(position: Int, expectedArgument: Any) {
    given {
        val argument = it.argumentArray[position]
        assertThat(argument).isNotNull()
        assertThat(argument).isEqualTo(expectedArgument)
    }
}
