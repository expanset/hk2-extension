package com.expanset.hk2.logging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor for methods or classes with {@link Loggable} annotation.
 */
@Service
@Contract
public class LoggableInterceptor implements MethodInterceptor {
	
	@Inject
	protected ServiceLocator serviceLocator;
	
	private final ConcurrentMap<String, Loggable> cache = new ConcurrentHashMap<>();

	@Override
	public Object invoke(MethodInvocation invocation) 
			throws Throwable {
		final Class<?> targetClass = invocation.getThis().getClass();
		final String key = targetClass.getCanonicalName() + "." + invocation.getMethod().getName();
		final Loggable loggable = cache.computeIfAbsent(key, (keyValue) -> {
			Loggable result = invocation.getMethod().getAnnotation(Loggable.class);
			if(result != null) {
				return result;
			}			
			return targetClass.getAnnotation(Loggable.class);
		});
		if(loggable == null) {
			return invocation.proceed();
		}
		
		ProfilerService profileService;
		if(StringUtils.isNotEmpty(loggable.service())) {
			profileService = serviceLocator.getService(ProfilerService.class, loggable.service());
		} else {
			profileService = serviceLocator.getService(ProfilerService.class);
		}
		
		final Logger log = LoggerFactory.getLogger(targetClass);
		try(AutoCloseable scope = profileService.startScope(
				log, 
				StringUtils.isNotEmpty(loggable.name()) ? loggable.name() : invocation.getMethod().getName(), 
				loggable.value(), 
				null, 
				loggable.idType(),
				loggable.measure())) {
			return invocation.proceed();
		}
	}
}
