package com.expanset.hk2.scheduling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Annotation to setup schedule for schedule driven services.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Qualifier
public @interface ScheduleDriven {
	
	/**
	 * @return Job name.
	 */
	String name();
	
	/**
	 * @return Job group.
	 */
	String group();
	
	/**
	 * @return Embedded schedule as "cron" expression.
	 */
	String expression() default "";
	
	/**
	 * @return Configuration property name for schedule as "cron" expression (you should register configuration access).
	 */
	String expressionProperty() default "";
}
