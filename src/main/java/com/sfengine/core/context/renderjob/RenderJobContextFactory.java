package com.sfengine.core.context.renderjob;

import com.sfengine.core.context.ContextFactory;
import com.sfengine.core.resources.Asset;

public class RenderJobContextFactory extends ContextFactory<RenderJobContext> {

    public static final String CONTEXT_IDENTIFIER = RenderJobContextFactory.class.getSimpleName();
    public static final String SUBASSET_NAME = "RenderJobContexts";

    @Override
    public String getContextIdentifier() {
        return CONTEXT_IDENTIFIER;
    }

    @Override
    public Asset getSubasset(Asset asset) {
        return asset.getSubAsset(SUBASSET_NAME);
    }
}
