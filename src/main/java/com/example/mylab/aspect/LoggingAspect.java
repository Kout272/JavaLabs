package com.example.mylab.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.example.mylab.controller.*.*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        logger.info("==> Calling: {}.{}()",
                joinPoint.getTarget().getClass().getSimpleName(),
                methodName);

        try {
            Object result = joinPoint.proceed();
            logger.info("<== Success: {}.{}()",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    methodName);
            return result;
        } catch (Exception ex) {
            logger.error("<== Error in {}.{}(): {}",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    methodName,
                    ex.getMessage());
            throw ex;
        }
    }
}