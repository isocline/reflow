package isocline.reflow;

import isocline.reflow.event.WorkEventFactory;
import isocline.reflow.flow.func.WorkEventConsumer;
import isocline.reflow.flow.func.WorkEventPublisher;

public class Re {

    public static Plan flow(FlowableWork work) {
        return FlowProcessor.core().reflow(work);
    }




    public static Plan task(Runnable runnable) {
        return FlowProcessor.core().task(runnable);
    }


    public static Plan task(WorkEventConsumer work) {
        return FlowProcessor.core().task(work);
    }

    public static Plan task(Work work) {
        return FlowProcessor.core().task(work);
    }


    public static Plan task(Work work, String... eventNames) {

        return FlowProcessor.core().task(work, eventNames);
    }




    public static FlowProcessor quest(WorkEvent event) {
        return FlowProcessor.core().emit(event);
    }

    public static WorkEvent quest(String evnetName, Object input) {
        WorkEvent event = WorkEventFactory.createOrigin(evnetName);
        try {
            WorkEventPublisher consumer = e -> {
                e.put("input", input);
            };

            consumer.accept(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        FlowProcessor.core().emit(evnetName, event);

        return event;
    }


    public static WorkEvent quest(String evnetName, WorkEventPublisher consumer) {
        WorkEvent event = WorkEventFactory.createOrigin(evnetName);
        try {
            consumer.accept(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        FlowProcessor.core().emit(evnetName, event);

        return event;
    }

    public static WorkEvent quest(String evnetName, WorkEventPublisher eventPublisher, WorkEventConsumer eventConsumer) {
        WorkEvent event = WorkEventFactory.createOrigin(evnetName);

        event.subscribe(eventConsumer);
        try {
            eventPublisher.accept(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        FlowProcessor pr = FlowProcessor.core().emit(evnetName, event);

        //event.block();

        return event;
    }



}
