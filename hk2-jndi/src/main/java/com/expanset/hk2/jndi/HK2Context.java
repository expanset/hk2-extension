package com.expanset.hk2.jndi;

import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.Reference;

import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.NamedImpl;

import com.expanset.jndi.InMemoryContext;

public class HK2Context extends InMemoryContext {

	public static final String SERVICE_LOCATOR_PROPERTY = HK2Context.class.getName() + ".serviceLocator"; 

	protected final ServiceLocator serviceLocator;
	
	protected HK2Context(Hashtable<?, ?> environment, Context parent, String name) 
			throws NamingException {
		super(environment, parent, name);
		
		if(environment == null || !environment.containsKey(SERVICE_LOCATOR_PROPERTY)) {
			throw new NamingException("Need SERVICE_LOCATOR_PROPERTY in environment");
		}
		
		serviceLocator = (ServiceLocator)environment.get(SERVICE_LOCATOR_PROPERTY);
	}
	
	@Override
	public Object lookup(Name name) 
			throws NamingException {
		if(name == null || name.isEmpty()) {
			return super.lookup(name);
		}
		
		Object obj = lookupImpl(name);
		if(obj == null) {
			synchronized (this) {
				obj = super.lookupImpl(name);
				if(obj == null) {
					final List<ServiceHandle<?>> services = 
							serviceLocator.getAllServiceHandles(new NamedImpl(name.toString()));	
					if(services.size() > 1) {
						throw new NamingException("Multiple services found for: " + name.toString());
					}
					if(services.size() == 1) {
						add(name, services.get(0), true);
					}
				}
			}
		}
		
		if(obj == null) {
			throw new NameNotFoundException(name.toString());
		}
		if (obj instanceof Reference) {
			throw new OperationNotSupportedException("References not supported");
		}
		if(obj instanceof ServiceHandle<?>) {
			obj = ((ServiceHandle<?>)obj).getService();
		}		
				
		return obj;
	}	

	@Override
	protected InMemoryContext createContext(Context parent, String name) 
			throws NamingException {
		return new HK2Context(environment, parent, name);
	}	
	
	@Override
	protected void closeObject(Object obj) {
		if(obj instanceof ServiceHandle) {
			((ServiceHandle<?>)obj).destroy();
		} else {
			super.closeObject(obj);
		}
	}
}