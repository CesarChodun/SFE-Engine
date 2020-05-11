package com.sfengine.core.context.device;

import com.sfengine.core.context.ContextFactory;
import com.sfengine.core.resources.Asset;

public class DeviceContextFactory extends ContextFactory<DeviceContext> {

    public static final String CONTEXT_IDENTIFIER = DeviceContextFactory.class.getSimpleName();
    public static final String SUBASSET_NAME = "DeviceContexts";

    @Override
    public String getContextIdentifier() {
        return CONTEXT_IDENTIFIER;
    }

    @Override
    public Asset getSubasset(Asset asset) {
        return asset.getSubAsset(SUBASSET_NAME);
    }
}
