package com.expanset.hk2.utils;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Scope;

import org.glassfish.hk2.api.Proxiable;

/**
 * PerCall is the scope for objects that are created every time when their methods are called. PerCall objects
 * will be destroyed whenever their methods are finished or they are destroyed explicitly
 * with the {@link org.glassfish.hk2.api.ServiceHandle#destroy()} method.
 */
@Documented
@Retention(RUNTIME)
@Scope 
@Proxiable
@Target( { TYPE, METHOD })
public @interface PerCall {

}
