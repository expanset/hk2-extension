package com.expanset.hk2.scheduling;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Ability to use schedule driven services (based on Quartz Scheduler). 
 */
public class ScheduleDrivenBinder extends AbstractBinder {

	@Override
	protected void configure() {
		addActiveDescriptor(ScheduleDrivenService.class);
	}
}
