package isocline.reflow;

import isocline.reflow.event.WorkEventFactory;
import isocline.reflow.flow.func.WorkEventConsumer;

public class Re {

    public static Plan flow(FlowableWork work) {
        return FlowProcessor.core().reflow(work);
    }

    public static Plan flow(WorkFlow flow) {
        return FlowProcessor.core().reflow(flow);
    }

    public static Plan call(Runnable runnable) {
        return FlowProcessor.core().task(runnable);
    }


    public static Plan call(WorkEventConsumer work) {
        return FlowProcessor.core().task(work);
    }

    public static Plan call(Work work) {
        return FlowProcessor.core().task(work);
    }


    public static Plan call(Work work, String... eventNames) {

        return FlowProcessor.core().task(work, eventNames);
    }


    public static FlowProcessor quest(WorkEvent event) {
        return FlowProcessor.core().emit(event);
    }

    public static WorkEvent quest(String evnetName, Object input) {
        WorkEvent event = WorkEventFactory.createOrigin(evnetName);
        try {
            WorkEventConsumer consumer = e -> {
                e.put("input", input);
            };

            consumer.accept(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        FlowProcessor.core().emit(evnetName, event);

        return event;
    }


    public static WorkEvent quest(String eventName, WorkEventConsumer consumer) {
        WorkEvent event = WorkEventFactory.createOrigin(eventName);
        try {
            consumer.accept(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        FlowProcessor.core().emit(eventName, event);

        return event;
    }

    public static WorkEvent quest(String eventName, WorkEventConsumer eventPublisher, WorkEventConsumer eventConsumer) {
        WorkEvent event = WorkEventFactory.createOrigin(eventName);

        event.subscribe(eventConsumer);
        try {
            eventPublisher.accept(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        FlowProcessor pr = FlowProcessor.core().emit(eventName, event);

        //event.block();

        return event;
    }


    public static WorkEvent ceive(String eventName, String targetEventName, WorkEventConsumer eventConsumer) {

        WorkEvent e = WorkEventFactory.createOrigin();
        e.setFireEventName(targetEventName);

        eventConsumer.accept(e);


        FlowProcessor.core().emit(eventName, targetEventName, e);


        return e;
    }


}
