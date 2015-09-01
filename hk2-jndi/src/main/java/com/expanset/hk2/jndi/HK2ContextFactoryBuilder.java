package com.expanset.hk2.jndi;

import java.util.Hashtable;

import javax.annotation.Nonnull;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

import org.apache.commons.lang3.Validate;
import org.glassfish.hk2.api.ServiceLocator;

public class HK2ContextFactoryBuilder implements InitialContextFactoryBuilder  {
	
	private final ServiceLocator serviceLocator;
	
	public HK2ContextFactoryBuilder(@Nonnull ServiceLocator serviceLocator) {
		Validate.notNull(serviceLocator, "serviceLocator");
		
		this.serviceLocator = serviceLocator;
	}
	
	public static void install(@Nonnull ServiceLocator serviceLocator) 
			throws NamingException {
		Validate.notNull(serviceLocator, "serviceLocator");
		
		NamingManager.setInitialContextFactoryBuilder(new HK2ContextFactoryBuilder(serviceLocator));
	}

	@Override
	public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) 
			throws NamingException {
		return new HK2ContextFactory(serviceLocator);
	}
}
