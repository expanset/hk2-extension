package com.expanset.hk2.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.glassfish.hk2.api.InjectionPointIndicator;

/**
 * Integer field annotation for retrieving filed's value from configuration.
 * <p>Example:</p>
 * <pre>
 * {@literal @}ConfiguredLong("page.size")
 * private long pageSize;
 * 
 * {@literal @}ConfiguredLong("page.size")
 * private Supplier&lt;Long&gt; pageSize;
 * </pre>
 */
@Inherited
@InjectionPointIndicator
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface ConfiguredLong {

	/**
	 * @return Key for value in configuration file.
	 */
	String value();

	/**
	 * @return Default value if it is not set in configuration file.
	 */	
	long def() default 0;
	
	/**
	 * @return Value is required.
	 */
	boolean required() default false;	
}
