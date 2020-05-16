package com.sfengine.components.contexts.renderjob;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextFactoryProvider;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.context.renderjob.RenderJobContextFactory;
import com.sfengine.core.rendering.factories.CommandBufferFactory;

import java.util.Dictionary;

public class BasicRenderJobContextFactory {

    public static BasicRenderJobContext createContext(String name, CommandBufferFactory cmdFactory, ContextDictionary dict) {
        BasicRenderJobContext context =
                new BasicRenderJobContext(name, RenderJobContextFactory.CONTEXT_IDENTIFIER, cmdFactory);
        RenderJobContextFactory factory =
                ContextFactoryProvider.getFactory(
                        RenderJobContextFactory.CONTEXT_IDENTIFIER, RenderJobContextFactory.class);
        factory.putContext(context);
        return context;
    }

}
