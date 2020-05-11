package com.sfengine.components.contexts.queue;

import com.sfengine.core.context.*;
import com.sfengine.core.context.queue.QueueContextFactory;

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
