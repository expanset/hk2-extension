package com.expanset.hk2.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.configuration.Configuration;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jvnet.hk2.annotations.Service;

/**
 * Injection binder for using annotations for the object fields that will be retrieved
 * from configuration.
 * <p>Example:</p>
 * <pre>
 * {@literal @}ConfiguredBoolean("db.enabled")
 * private boolean enabled;
 * 
 * {@literal @}ConfiguredBoolean("db.enabled")
 * private Supplier&lt;Boolean&gt; enabled;
 * </pre>
 * To use configuration in the file you should register {@link Configuration} in the service locator.
 */
public class ConfiguredFieldsBinder extends AbstractBinder {

	@Override
	protected void configure() {
		bind(ConfiguredStringResolver.class)
			.to(new TypeLiteral<InjectionResolver<ConfiguredString>>(){})
			.in(Singleton.class);		
		bind(ConfiguredIntegerResolver.class)
			.to(new TypeLiteral<InjectionResolver<ConfiguredInteger>>(){})
			.in(Singleton.class);		
		bind(ConfiguredLongResolver.class)
			.to(new TypeLiteral<InjectionResolver<ConfiguredLong>>(){})
			.in(Singleton.class);		
		bind(ConfiguredBooleanResolver.class)
			.to(new TypeLiteral<InjectionResolver<ConfiguredBoolean>>(){})
			.in(Singleton.class);		
		
		addActiveDescriptor(ConfiguredInstanceLifecycleListener.class);
	}
	
	@Service
	protected static class ConfiguredBooleanResolver implements InjectionResolver<ConfiguredBoolean> {

		@Inject
		protected Provider<Configuration> config;	
		
		@Override
		public Object resolve(Injectee injectee, ServiceHandle<?> root) {
			final ConfiguredBoolean ann = injectee.getParent().getAnnotation(ConfiguredBoolean.class);
			
			assert ann != null;

			if(injectee.getRequiredType() instanceof ParameterizedType) {
				final Type fieldType = ((ParameterizedType)injectee.getRequiredType()).getRawType();
				if(fieldType.equals(Supplier.class) || fieldType.equals(ConfiguredReloadable.class)) {
					return new ConfiguredReloadable<Boolean>(config.get(), ann.value(), (Boolean)ann.def(), ann.required());
				}
			}
			
			return ann.required() ? config.get().getBoolean(ann.value()) : config.get().getBoolean(ann.value(), ann.def());			
		}
		
		@Override
		public boolean isConstructorParameterIndicator() {
			return true;
		}
		
		@Override
		public boolean isMethodParameterIndicator() {
			return true;
		}
	}

	@Service
	protected static class ConfiguredIntegerResolver implements InjectionResolver<ConfiguredInteger> {

		@Inject
		protected Provider<Configuration> config;	
		
		@Override
		public Object resolve(Injectee injectee, ServiceHandle<?> root) {
			final ConfiguredInteger ann = injectee.getParent().getAnnotation(ConfiguredInteger.class);
			
			assert ann != null;
			
			if(injectee.getRequiredType() instanceof ParameterizedType) {
				final Type fieldType = ((ParameterizedType)injectee.getRequiredType()).getRawType();
				if(fieldType.equals(Supplier.class) || fieldType.equals(ConfiguredReloadable.class)) {
					return new ConfiguredReloadable<Integer>(config.get(), ann.value(), (Integer)ann.def(), ann.required());
				}
			}

			return ann.required() ? config.get().getInt(ann.value()) : config.get().getInt(ann.value(), ann.def());			
		}
		
		@Override
		public boolean isConstructorParameterIndicator() {
			return true;
		}
		
		@Override
		public boolean isMethodParameterIndicator() {
			return true;
		}
	}
	
	@Service
	protected static class ConfiguredLongResolver implements InjectionResolver<ConfiguredLong> {

		@Inject
		protected Provider<Configuration> config;	
		
		@Override
		public Object resolve(Injectee injectee, ServiceHandle<?> root) {
			final ConfiguredLong ann = injectee.getParent().getAnnotation(ConfiguredLong.class);
			
			assert ann != null;
					
			if(injectee.getRequiredType() instanceof ParameterizedType) {
				final Type fieldType = ((ParameterizedType)injectee.getRequiredType()).getRawType();
				if(fieldType.equals(Supplier.class) || fieldType.equals(ConfiguredReloadable.class)) {
					return new ConfiguredReloadable<Long>(config.get(), ann.value(), (Long)ann.def(), ann.required());
				}
			}
			
			return ann.required() ? config.get().getLong(ann.value()) : config.get().getLong(ann.value(), ann.def());			
		}
		
		@Override
		public boolean isConstructorParameterIndicator() {
			return true;
		}
		
		@Override
		public boolean isMethodParameterIndicator() {
			return true;
		}
	}	
			
	@Service
	protected static class ConfiguredStringResolver implements InjectionResolver<ConfiguredString> {

		@Inject
		protected Provider<Configuration> config;	
		
		@Override
		public Object resolve(Injectee injectee, ServiceHandle<?> root) {
			final ConfiguredString ann = injectee.getParent().getAnnotation(ConfiguredString.class);
			
			assert ann != null;
			
			if(injectee.getRequiredType() instanceof ParameterizedType) {
				final Type fieldType = ((ParameterizedType)injectee.getRequiredType()).getRawType();
				if(fieldType.equals(Supplier.class) || fieldType.equals(ConfiguredReloadable.class)) {
					return new ConfiguredReloadable<String>(config.get(), ann.value(), (String)ann.def(), ann.required());
				}
			}
			
			return  ann.required() ? config.get().getString(ann.value()) : config.get().getString(ann.value(), ann.def());			
		}
		
		@Override
		public boolean isConstructorParameterIndicator() {
			return true;
		}
		
		@Override
		public boolean isMethodParameterIndicator() {
			return true;
		}
	}	
}
