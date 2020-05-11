package com.sfengine.core.context.queuefamily;

import com.sfengine.core.context.ContextFactory;
import com.sfengine.core.resources.Asset;

public class QueueFamilyContextFactory extends ContextFactory<QueueFamilyContext> {
    public static final String CONTEXT_IDENTIFIER = QueueFamilyContextFactory.class.getSimpleName();
    public static final String SUBASSET_NAME = "QueueFamilyContexts";

    @Override
    public String getContextIdentifier() {
        return CONTEXT_IDENTIFIER;
    }

    @Override
    public Asset getSubasset(Asset asset) {
        return asset.getSubAsset(SUBASSET_NAME);
    }
}
