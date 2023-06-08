package it.polito.wa2.server.observer

import jakarta.validation.constraints.NotNull
import org.aspectj.lang.ProceedingJoinPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class AbstractLogAspect {
    fun logBefore(joinPoint: ProceedingJoinPoint) {
        val logInfo: LogInfo = getLogInfo(joinPoint)
        // this make the logger print the right classType
        val log: Logger = LoggerFactory.getLogger(logInfo.declaringType)
        log.info("[{}.{}] start ({})", logInfo.className, logInfo.annotatedMethodName, logInfo.args)
    }

    fun logAfter(joinPoint: ProceedingJoinPoint) {
        val logInfo: LogInfo = getLogInfo(joinPoint)
        val log: Logger = LoggerFactory.getLogger(logInfo.declaringType)
        log.info("[{}.{}] end", logInfo.className, logInfo.annotatedMethodName)
    }

    private fun getLogInfo(joinPoint: ProceedingJoinPoint): LogInfo {
        val signature = joinPoint.signature
        val declaringType = signature.declaringType
        val className = declaringType.simpleName
        val annotatedMethodName = signature.name
        val args: Array<Any>? = joinPoint.args
        return LogInfo(declaringType, className, annotatedMethodName, args)
    }

    private data class LogInfo(
        @field:NotNull @param:NotNull
        val declaringType: Class<*>,
        @field:NotNull @param:NotNull
        val className: String,
        @field:NotNull @param:NotNull
        val annotatedMethodName: String,
        val args: Array<Any>?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LogInfo

            if (declaringType != other.declaringType) return false
            if (className != other.className) return false
            if (annotatedMethodName != other.annotatedMethodName) return false
            if (args != null) {
                if (other.args == null) return false
                if (!args.contentEquals(other.args)) return false
            } else if (other.args != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = declaringType.hashCode()
            result = 31 * result + className.hashCode()
            result = 31 * result + annotatedMethodName.hashCode()
            result = 31 * result + (args?.contentHashCode() ?: 0)
            return result
        }
    }
}