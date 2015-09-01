package com.expanset.hk2.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.glassfish.hk2.api.InjectionPointIndicator;

/**
 * Boolean field annotation for retrieving filed's value from configuration.
 * <p>Example:</p>
 * <pre>
 * {@literal @}ConfiguredBoolean("db.enabled")
 * private boolean enabled;
 * 
 * {@literal @}ConfiguredBoolean("db.enabled")
 * private Supplier&lt;Boolean&gt; enabled;
 * </pre>
 */
@Inherited
@InjectionPointIndicator
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface ConfiguredBoolean {

	/**
	 * @return Key for value in configuration.
	 */
	String value();

	/**
	 * @return Default value if it is not set in configuration.
	 */
	boolean def() default false;
	
	/**
	 * @return Value is required.
	 */
	boolean required() default false;	
}
