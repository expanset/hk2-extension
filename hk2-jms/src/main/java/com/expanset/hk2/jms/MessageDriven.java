package com.expanset.hk2.jms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;
import javax.jms.Session;

/**
 * Annotation to control receiving of JMS messages in message driven services.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Qualifier
public @interface MessageDriven {

	/**
	 * @return Name of Queue or Topic.
	 */
	String destination();
	
	/**
	 * @return Name of connection factory that will be used to retrieve connection factory.
	 */
	String connectionFactory() default "";

	/**
	 * @return Session is transacted or not.
	 */
	boolean transacted() default false;

	/**
	 * @return Message acknowledge mode.
	 */
	int acknowledgeMode() default Session.AUTO_ACKNOWLEDGE;

	/**
	 * @return JMS message selector expression.
	 */
	String messageSelector() default "";
	
	/**
	 * @return Type of destination: Queue or Topic.
	 */
	DestinationType destinationType() default DestinationType.QUEUE;
	
	/**
	 * @return Topic subscription durability. Only for Topics.
	 */
	boolean subscriptionDurability() default false;

	/**
	 * @return Name used to identify this subscription
	 */
	String subscriptionName() default "";

	/**
	 * @return If set, inhibits the delivery of messages published by its own connection.
	 */
	boolean subscriptionNoLocal() default false;

	/**
	 * @return Client identifier.
	 */
	String clientId() default "";
}
