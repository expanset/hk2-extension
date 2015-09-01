package com.expanset.hk2.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.glassfish.hk2.api.InjectionPointIndicator;

/**
 * {@link String} field annotation for retrieving filed's value from the configuration.
 * <p>Example:</p>
 * <pre>
 * {@literal @}ConfiguredString("page.name")
 * private String pageName;
 * 
 * {@literal @}ConfiguredString("page.name")
 * private Supplier&lt;String&gt; pageName;
 * </pre>
 */
@Inherited
@InjectionPointIndicator
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface ConfiguredString {

	/**
	 * @return Key for value in configuration.
	 */
	String value();

	/**
	 * @return Default value if it is not set in configuration.
	 */	
	String def() default "";
	
	/**
	 * @return Value is required.
	 */
	boolean required() default false;
}
