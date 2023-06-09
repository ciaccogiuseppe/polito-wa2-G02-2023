package it.polito.wa2.server.observer

import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationHandler
import io.micrometer.observation.aop.ObservedAspect
import org.aspectj.lang.ProceedingJoinPoint
import org.springframework.stereotype.Component

@Component
class AbstractObserveAroundMethodHandler: AbstractLogAspect(), ObservationHandler<ObservedAspect.ObservedAspectContext> {
    override fun onStart(context: ObservedAspect.ObservedAspectContext) {
        super.onStart(context)
        /* we can get much information (including class, arguments...)
        form ProceedingJoinPoint. */
        val joinPoint: ProceedingJoinPoint = context.proceedingJoinPoint
        super.logBefore(joinPoint)
    }

    override fun onStop(context: ObservedAspect.ObservedAspectContext) {
        super.onStop(context)
        val joinPoint = context.proceedingJoinPoint
        super.logAfter(joinPoint)
    }

    override fun supportsContext(context: Observation.Context): Boolean {
        /* required, otherwise they here will handle the
        non-spring bean method (e.g. handling http.server.requests)
        and throw a class cast exception. */
        return context is ObservedAspect.ObservedAspectContext
    }

}