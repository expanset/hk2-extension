package com.expanset.hk2.persistence.jpa;

import javax.inject.Singleton;
import javax.persistence.PersistenceContext;

import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import com.expanset.hk2.persistence.PersistenceBinder;

/**
 * Using JPA persistence engine (Java Persistence API). 
 * You may use Eclipselink, Hibernate or other JPA implementation.
 */
public class JpaPersistenceBinder extends PersistenceBinder {

	/**
	 * Common configuration property for setup default unit name.
	 */
	public final static String DEFAULT_UNIT_NAME = JpaPersistenceBinder.class.getName() + ".defaultUnitName";
	
	@Override
	protected void configure() {
		super.configure();
		
		addActiveDescriptor(JpaPersistenceContextFactoryCreator.class);
		
		bind(JpaPersistenceContextResolver.class)
			.to(new TypeLiteral<InjectionResolver<PersistenceContext>>(){})
			.in(Singleton.class);
	}
}
