package isocline.reflow;

import isocline.reflow.event.WorkEventKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class WorkHelper {


    public static Activity Reflow(FlowableWork workFlow) {
        return FlowProcessor.core().reflow(workFlow).activate().block();
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static List GetResultList(WorkEvent e) {

        WorkEvent event = e.origin();
        if (event == null) {
            event = e;
        }

        String resultKey = "result::" + event.hashCode();

        List list;
        synchronized (event) {
            list = (List) event.get(resultKey);
            if (list == null) {
                list = Collections.synchronizedList(new ArrayList<>());

                event.put(resultKey, list);
            }
        }

        return list;
    }


    public static Object Get(WorkEvent e) {

        if (e == null) {
            return null;
        }

        WorkEvent event = e.origin();
        if (event == null) {
            event = e;
        }

        String resultKey = "result::" + event.hashCode() + "<Mono>";


        return event.get(resultKey);

    }


    public static void Return(WorkEvent e, Object result) {

        WorkEvent event = e.origin();
        if (event == null) {
            event = e;
        }

        String resultKey = "result::" + event.hashCode();

        event.put(resultKey + "<Mono>", result);

        List list;
        synchronized (event) {
            list = (List) event.get(resultKey);
            if (list == null) {
                list = Collections.synchronizedList(new ArrayList<>());

                event.put(resultKey, list);
            }
        }

        list.add(result);
    }


    static void emitLocalEvent(Activity plan, WorkEvent event, String eventName, long delayTime) {

        emitLocalEvent(plan, event, eventName, delayTime, null);

    }

    static void emitLocalErrorEvent(Activity plan, WorkEvent event, String eventName, long delayTime, Throwable error) {
        if (eventName != null && eventName.indexOf(WorkEventKey.PREFIX_ERROR) == 0) {
            final WorkEvent we = event.createChild(eventName);
            if (error != null) {
                we.setThrowable(error);
            }

            we.setFireEventName(WorkFlow.ERROR);
            plan.emit(we, delayTime);

        }

    }

    static void emitLocalEvent(Activity plan, WorkEvent event, String eventName, long delayTime, Throwable error) {
        emitLocalEvent(plan, event, eventName, delayTime, error, null);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    static void emitLocalEvent(Activity plan, WorkEvent event, String eventName, long delayTime, Throwable error, Thread currentThread) {

        if (eventName == null) return;

        WorkEvent emitEvent;
        emitEvent = event.createChild(eventName);
        if (error == null) {
            //emitEvent = event.createChild(eventName);
        } else {
            //emitEvent = WorkEventFactory.createWithOrigin(eventName,event.origin());
            emitEvent.setThrowable(error);
        }

        if (currentThread != null) {
            emitEvent.setTimeoutThread(currentThread);
        }

        plan.emit(emitEvent, delayTime);

        //XLogger.getLogger(WorkHelper.class).error(" ______________________ "+eventName);


    }

    static void emitLocalEvent(Activity plan, WorkEvent event, String[] eventNames, long delayTime, Throwable error, Thread currentThread) {

        if (eventNames == null || delayTime < 1) return;

        for (String eventName : eventNames) {
            emitLocalEvent(plan, event, eventName, delayTime, error, currentThread);
        }
    }

    static void emitLocalEvent(Activity plan, WorkEvent event, String[] eventNames, long delayTime) {

        if (eventNames == null) return;

        for (String eventName : eventNames) {
            emitLocalEvent(plan, event, eventName, delayTime, null);
        }
    }

    static void emitLocalEvent(Activity plan, WorkEvent event, String[] eventNames, Throwable error) {

        if (eventNames == null) return;

        for (String eventName : eventNames) {
            emitLocalEvent(plan, event, eventName, 0, error);
        }
    }

}
