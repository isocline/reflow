package isocline.reflow;

import isocline.reflow.event.WorkEventFactory;
import isocline.reflow.flow.func.WorkEventConsumer;

public class Re {

    public static Plan flow(FlowableWork work) {
        return FlowProcessor.core().task(work);
    }

    public static Plan flow(WorkFlow flow) {
        return FlowProcessor.core().reflow(flow);
    }



    public static Plan flow(Class runClass) throws InstantiationException, IllegalAccessException  {
        return FlowProcessor.core().task(runClass);
    }


    public static Plan flow(Work work) {

        return FlowProcessor.core().task(work);
    }


    public static Plan flow(Work work, String... eventNames) {

        return FlowProcessor.core().task(work, eventNames);
    }


    public static Plan flow(PlanDescriptor descriptor, Work work) {

        return FlowProcessor.core().task(descriptor, work);
    }


    public static Plan peat(Runnable runnable) {
        return FlowProcessor.core().task(runnable);
    }

    public static Plan peat(WorkEventConsumer work) {
        return FlowProcessor.core().task(work);
    }



    public static ResultEvent quest(WorkEvent event) {
        FlowProcessor.core().emit(event);
        return event;
    }

    public static ResultEvent quest(String eventName, DataChannel dataChannel) {
        return quest(eventName, dataChannel, null);
    }

    public static ResultEvent quest(String eventName, Object input) {
        return quest(eventName, new DataChannel(input) , null);
    }

    public static ResultEvent quest(String eventName, DataChannel dataChannel, WorkEventConsumer consumer) {
        WorkEvent event = WorkEventFactory.createOrigin(eventName);
        event.dataChannel(dataChannel);

        int sp = eventName.indexOf("://");
        if(sp>0) {
            event.setFireEventName(eventName.substring(0, sp+3));
        }

        try {
            if(consumer!=null) {
                event.subscribe(consumer);
            }


            //consumer.accept(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        FlowProcessor.core().emit(event);

        return event;
    }

    public static ResultEvent quest(WorkEvent event, WorkEventConsumer consumer) {

        event.subscribe(consumer);

        FlowProcessor.core().emit(event);

        return event;
    }

    public static ResultEvent quest(String eventName, WorkEventConsumer consumer) {
        WorkEvent event = WorkEventFactory.createOrigin(eventName);
        try {
            consumer.accept(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        FlowProcessor.core().emit(eventName, event);

        return event;
    }

    public static ResultEvent quest(String eventName, WorkEventConsumer eventPublisher, WorkEventConsumer eventConsumer) {
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
