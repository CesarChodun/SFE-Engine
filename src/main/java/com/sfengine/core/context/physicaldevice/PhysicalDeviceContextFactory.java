package com.sfengine.core.context.physicaldevice;

import com.sfengine.core.context.ContextFactory;
import com.sfengine.core.resources.Asset;

public class PhysicalDeviceContextFactory extends ContextFactory<PhysicalDeviceContext> {

    public static final String CONTEXT_IDENTIFIER = PhysicalDeviceContextFactory.class.getSimpleName();
    public static final String SUBASSET_NAME = "PhysicalDeviceContexts";

    @Override
    public String getContextIdentifier() {
        return CONTEXT_IDENTIFIER;
    }

    @Override
    public Asset getSubasset(Asset asset) {
        return asset.getSubAsset(SUBASSET_NAME);
    }
}
