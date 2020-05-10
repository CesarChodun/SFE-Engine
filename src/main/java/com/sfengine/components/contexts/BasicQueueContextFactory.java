package com.sfengine.components.contexts;

import com.sfengine.core.context.*;

public class BasicQueueContextFactory {

    public static BasicQueueContext createQueueContext(String name, ContextDictionary dict) {
        QueueContextFactory factory =
                ContextFactoryProvider.getFactory(QueueContextFactory.CONTEXT_IDENTIFIER, QueueContextFactory.class);
        BasicQueueContext queueContext =
                new BasicQueueContext(name, factory.getContextIdentifier(), dict, 0);
        factory.putContext(queueContext);
        return queueContext;
    }

}
