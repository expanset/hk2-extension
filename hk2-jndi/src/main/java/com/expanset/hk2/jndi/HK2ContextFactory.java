package com.expanset.hk2.jndi;

import java.util.Hashtable;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.glassfish.hk2.api.ServiceLocator;

public class HK2ContextFactory implements InitialContextFactory {

	private final ServiceLocator serviceLocator;
	
	public HK2ContextFactory() {
		serviceLocator = null;
	}

	public HK2ContextFactory(ServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}
	
	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) 
			throws NamingException {
		final Hashtable<String, Object> environmentToUse = new Hashtable<>();
		if(environment != null) {
			for(Entry<?, ?> entry : environment.entrySet()) {
				environmentToUse.put(entry.getKey().toString(), entry.getValue());
			}
		}
		environmentToUse.putIfAbsent(HK2Context.SERVICE_LOCATOR_PROPERTY, serviceLocator);

		final Context initialContext = new HK2Context(environmentToUse, null, null);
		return initialContext;	
	}
}
