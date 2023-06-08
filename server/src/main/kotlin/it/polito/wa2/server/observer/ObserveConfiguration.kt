package it.polito.wa2.server.observer

import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.aop.ObservedAspect
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class ObserveConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObservedAspect::class)
    fun observedAspect(observationRegistry: ObservationRegistry?): ObservedAspect? {
        return ObservedAspect(observationRegistry!!)
    }
}