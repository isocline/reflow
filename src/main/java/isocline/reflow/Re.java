package isocline.reflow;

import isocline.reflow.event.WorkEventFactory;
import isocline.reflow.flow.func.WorkEventConsumer;

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


    public static FlowProcessor quest(String evnetName, WorkEventConsumer consumer) {
        WorkEvent event = WorkEventFactory.createOrigin(evnetName);
        try {
            consumer.accept(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return FlowProcessor.core().emit(evnetName, event);
    }
}
