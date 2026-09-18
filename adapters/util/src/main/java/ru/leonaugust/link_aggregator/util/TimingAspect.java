package ru.leonaugust.link_aggregator.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TimingAspect {

    @Around("@annotation(ru.leonaugust.link_aggregator.util.MeasureTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            String methodName = joinPoint.getSignature().toShortString();
            log.info("⏱ {} executed in {} ms", methodName, durationMs);
        }
    }
}