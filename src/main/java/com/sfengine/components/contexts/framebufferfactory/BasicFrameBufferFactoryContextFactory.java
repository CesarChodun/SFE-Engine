package com.sfengine.components.contexts.framebufferfactory;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextFactoryProvider;
import com.sfengine.core.context.framebufferfactory.FrameBufferFactoryContextFactory;

public class BasicFrameBufferFactoryContextFactory {

    public static BasicFrameBufferFactoryContext createFrameBufferFactoryContext(String name, ContextDictionary dict, long renderPass) {
        FrameBufferFactoryContextFactory factory =
                ContextFactoryProvider.getFactory(
                        FrameBufferFactoryContextFactory.CONTEXT_IDENTIFIER,
                        FrameBufferFactoryContextFactory.class);

        BasicFrameBufferFactoryContext context =
                new BasicFrameBufferFactoryContext(name, dict, renderPass);
        factory.putContext(context);
        return context;
    }
}
