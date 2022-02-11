@file:JvmName("KLog")

package de.solugo.klog

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

private val cache = hashMapOf<Class<*>, Logger>()

val Any.logger: Logger
    get() = when {
        this is Class<*> -> cache.computeIfAbsent(this) { LoggerFactory.getLogger(this) }
        this is KClass<*> -> this.java.logger
        else -> this.javaClass.logger
    }

inline fun Any.logWarn(t: Throwable? = null, message: () -> String) {
    this.logger.apply {
        if (isWarnEnabled) warn(message(), t)
    }
}

inline fun Any.logError(t: Throwable? = null, message: () -> String) {
    this.logger.apply {
        if (isErrorEnabled) error(message(), t)
    }
}

inline fun Any.logInfo(t: Throwable? = null, message: () -> String) {
    this.logger.apply {
        if (isInfoEnabled) info(message(), t)
    }
}

inline fun Any.logDebug(t: Throwable? = null, message: () -> String) {
    this.logger.apply {
        if (isDebugEnabled) debug(message(), t)
    }
}
