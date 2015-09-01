package com.expanset.hk2.persistence.ormlite;

import javax.inject.Singleton;

import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import com.expanset.hk2.persistence.PersistenceBinder;

/**
 * Using ORMlite persistence engine. 
 */
public class OrmlitePersistenceBinder extends PersistenceBinder {

	@Override
	protected void configure() {
		super.configure();
		
		addActiveDescriptor(OrmlitePersistenceContextFactoryCreator.class);
		
		bind(OrmlitePersistenceContextResolver.class)
			.to(new TypeLiteral<InjectionResolver<OrmlitePersistenceContext>>(){})
			.in(Singleton.class);			
	}
}
