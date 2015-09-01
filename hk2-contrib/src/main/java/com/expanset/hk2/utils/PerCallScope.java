package com.expanset.hk2.utils;

import java.lang.annotation.Annotation;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.ServiceHandle;
import org.jvnet.hk2.annotations.Service;

/**
 * PerCall scope support.
 */
@Service
public class PerCallScope implements Context<PerCall> {

    @Override
    public Class<? extends Annotation> getScope() {
        return PerCall.class;
    }

    @Override
    public <T> T findOrCreate(ActiveDescriptor<T> activeDescriptor, ServiceHandle<?> root) {
    	assert activeDescriptor != null;
    	
        return activeDescriptor.create(root);
    }

    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        return false;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean supportsNullCreation() {
        return true;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        // Do nothing, this is a special case.
    }
}