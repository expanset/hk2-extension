package com.expanset.hk2.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods, which are needed to be profiled.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Loggable {

	/**
	 * @return Log level for log output.
	 */
	LogLevel value() default LogLevel.DEFAULT;
	
	/**
	 * @return Name of current operation.
	 */
	String name() default "";

	/**
	 * @return Type of correlation ID, which will be generated for method call.
	 */
	CorrelationIdType idType() default CorrelationIdType.NONE;
	
	/**
	 * @return true - calculates method duration in milliseconds.
	 */
	boolean measure() default false;
	
	/**
	 * @return Name of profiling service (if named profile service will be used).
	 */
	String service() default "";
}
