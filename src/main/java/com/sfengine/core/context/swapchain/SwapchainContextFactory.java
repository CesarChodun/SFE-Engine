package com.sfengine.core.context.swapchain;

import com.sfengine.core.context.ContextFactory;
import com.sfengine.core.context.device.DeviceContextFactory;
import com.sfengine.core.resources.Asset;

public class SwapchainContextFactory extends ContextFactory<SwapchainContext> {

    public static final String CONTEXT_IDENTIFIER = SwapchainContextFactory.class.getSimpleName();
    public static final String SUBASSET = "swapchains";

    @Override
    public String getContextIdentifier() {
        return CONTEXT_IDENTIFIER;
    }

    @Override
    public Asset getSubasset(Asset asset) {
        return asset.getSubAsset(SUBASSET);
    }
}
