package com.expanset.hk2.config;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.event.EventSource;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InstanceLifecycleEvent;
import org.glassfish.hk2.api.InstanceLifecycleEventType;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Service;

/**
 * Gives opportunity for classes that implements {@link ConfigurationReloadListener} 
 * listening for configuration changing.
 */
@Service
public class ConfiguredInstanceLifecycleListener implements InstanceLifecycleListener {

	@Inject
	protected Configuration config;	
	
	@Override
	public Filter getFilter() {
		return BuilderHelper.createContractFilter(ConfigurationReloadListener.class.getName());
	}

	@Override
	public void lifecycleEvent(InstanceLifecycleEvent lifecycleEvent) {
		if(lifecycleEvent.getEventType() == InstanceLifecycleEventType.POST_PRODUCTION) {
			final List<EventSource> eventSources = getEventSources();
			for(EventSource eventSource : eventSources) {
				eventSource.addConfigurationListener(new ConfigurationListenerImpl(
						(ConfigurationReloadListener)lifecycleEvent.getLifecycleObject()));
			}
		} else if(lifecycleEvent.getEventType() == InstanceLifecycleEventType.PRE_DESTRUCTION) {
			final List<EventSource> eventSources = getEventSources();
			for(EventSource eventSource : eventSources) {
				removeListeners(eventSource);
			}
		}
	}
	
	private void removeListeners(EventSource eventSource) {
		for(ConfigurationListener listener : eventSource.getConfigurationListeners()) {
			if(listener instanceof ConfigurationListenerImpl) {
				eventSource.removeConfigurationListener(listener);
			}
		}
	}
	
	private List<EventSource> getEventSources() {
		final List<EventSource> result = new ArrayList<>();
		if(config instanceof CompositeConfiguration) {
			final CompositeConfiguration compositeConfig = (CompositeConfiguration)config;
			for(int i = 0; i < compositeConfig.getNumberOfConfigurations(); i++) {
				final Configuration childConfig = compositeConfig.getConfiguration(i); 
				if(childConfig instanceof EventSource) {
					result.add((EventSource)childConfig);
				}
			}
		} else if(config instanceof EventSource) {
			result.add((EventSource)config);
		} else {
			assert false : "Unknown type";
		}
		
		return result;
	}

	private static class ConfigurationListenerImpl implements ConfigurationListener {

		private final ConfigurationReloadListener listener;
		
		public ConfigurationListenerImpl(ConfigurationReloadListener listener) {
			assert listener != null;
			
			this.listener = listener;
		}
		
		@Override
		public void configurationChanged(ConfigurationEvent event) {
			if(event.getType() == AbstractFileConfiguration.EVENT_RELOAD && !event.isBeforeUpdate()) {
				listener.configurationReloaded();
			}			
		}
	}
}
