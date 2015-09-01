package com.expanset.hk2.persistence.transactions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InterceptionService;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Service;

/**
 * Register interceptor for methods, that are annotated by {@link Transactional}.
 */
@Service
public class TransactionalInterceptorService implements InterceptionService {

	protected final List<MethodInterceptor> interceprors = new ArrayList<>();
	
	@Inject
	protected TransactionalInterceptor interceptor;	
	
	@PostConstruct
	public void initialize() {
		interceprors.add(interceptor);
	}
	
	@Override
	public Filter getDescriptorFilter() {
		return BuilderHelper.allFilter();
	}

	@Override
	public List<MethodInterceptor> getMethodInterceptors(Method method) {
        if (method.isAnnotationPresent(Transactional.class) || 
        		method.getDeclaringClass().isAnnotationPresent(Transactional.class)) {
            return interceprors;
        }
        
		return null;
	}

	@Override
	public List<ConstructorInterceptor> getConstructorInterceptors(Constructor<?> constructor) {
		return null;
	}
}
