package com.expanset.hk2.jms;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Ability to use message driven services (JMS 1.1). 
 */
public class MessageDrivenBinder extends AbstractBinder {

	@Override
	protected void configure() {
		addActiveDescriptor(MessageDrivenService.class);	
	}
}

