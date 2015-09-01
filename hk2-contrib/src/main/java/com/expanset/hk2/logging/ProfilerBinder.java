package com.expanset.hk2.logging;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InterceptionService;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jvnet.hk2.annotations.Service;

/**
 * HK2 binder for methods profiling.
 */
public class ProfilerBinder extends AbstractBinder {
	
	@Override
	protected void configure() {
		registerProfilerService();
		registerLoggableInterceptionService();
	}
	
	protected void registerProfilerService() {
		addActiveDescriptor(ProfilerService.class);
	}

	protected void registerLoggableInterceptionService() {
		addActiveDescriptor(LoggableInterceptor.class);
		addActiveDescriptor(LoggableInterceptionService.class);
	}
	
	@Service
	protected static class LoggableInterceptionService implements InterceptionService {

		private final List<MethodInterceptor> interceptors = new ArrayList<>(1);

		@Inject
		public LoggableInterceptionService(LoggableInterceptor interceptor) {
			interceptors.add(interceptor);
		} 
		
		@Override
		public Filter getDescriptorFilter() {
			return BuilderHelper.allFilter();
		}

		@Override
		public List<MethodInterceptor> getMethodInterceptors(Method method) {
	        if (method.getDeclaringClass().isAnnotationPresent(Loggable.class) 
	        		|| method.isAnnotationPresent(Loggable.class)) {
	        	return interceptors;
	        }
	        
			return null;
		}

		@Override
		public List<ConstructorInterceptor> getConstructorInterceptors(Constructor<?> constructor) {
			return null;
		}
	}	
}
