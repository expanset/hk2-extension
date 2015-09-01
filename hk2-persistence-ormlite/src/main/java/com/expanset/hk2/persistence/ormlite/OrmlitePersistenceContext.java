package com.expanset.hk2.persistence.ormlite;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.glassfish.hk2.api.InjectionPointIndicator;

/**
 * Injection point annotation for {@link com.j256.ormlite.support.ConnectionSource}.
 * <p>Example:</p>
 * <pre>
 * {@literal @}OrmlitePersistenceContext
 * private ConnectionSource connectionSource;
 * </pre>
 */
@Inherited
@InjectionPointIndicator
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface OrmlitePersistenceContext {
}
