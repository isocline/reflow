package isocline.reflow;

import isocline.reflow.event.WorkEventFactory;
import isocline.reflow.flow.func.WorkEventConsumer;
import isocline.reflow.flow.func.WorkEventPublisher;

public class Re {

    public static Plan flow(Runnable runnable) {
        return FlowProcessor.core().reflow(runnable);
    }

    public static Plan flow(Work work) {
        return FlowProcessor.core().reflow(work);
    }


    public static Plan flow(FlowableWork work) {
        return FlowProcessor.core().reflow(work);
    }


    public static Plan flow(Work work, String... eventNames) {

        return FlowProcessor.core().reflow(work, eventNames);
    }

    public static FlowProcessor quest(WorkEvent event) {
        return FlowProcessor.core().emit(event);
    }


    public static FlowProcessor quest(String evnetName, WorkEventPublisher consumer) {
        WorkEvent event = WorkEventFactory.createOrigin(evnetName);
        try {
            consumer.accept(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return FlowProcessor.core().emit(evnetName, event);
    }

    public static FlowProcessor quest(String evnetName, WorkEventPublisher eventPublisher, WorkEventConsumer eventConsumer) {
        WorkEvent event = WorkEventFactory.createOrigin(evnetName);

        event.setCosumer(eventConsumer);
        try {
            eventPublisher.accept(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        FlowProcessor pr= FlowProcessor.core().emit(evnetName, event);

        //event.block();

        return pr;
    }
}
