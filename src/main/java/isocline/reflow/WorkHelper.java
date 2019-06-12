package isocline.reflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkHelper {


    public static Plan reflow(FlowableWork workFlow) {
        return FlowProcessor.core().reflow(workFlow);
    }

    public static List GetResultList(WorkEvent e) {

        WorkEvent event = e.origin();
        if (event == null) {
            event = e;
        }

        String resultKey = "result::" + event.hashCode();

        List list = null;
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

        List list = null;
        synchronized (event) {
            list = (List) event.get(resultKey);
            if (list == null) {
                list = Collections.synchronizedList(new ArrayList<>());

                event.put(resultKey, list);
            }
        }

        list.add(result);
    }


    static void emitLocalEvent(ActivatedPlan plan, WorkEvent event, String eventName, long delayTime) {

        emitLocalEvent(plan, event, eventName, delayTime, null);

    }


    static void emitLocalEvent(ActivatedPlan plan, WorkEvent event, String eventName, long delayTime, Throwable error) {

        if(eventName==null) return;

        WorkEvent emitEvent = null;
        emitEvent = event.createChild(eventName);
        if(error==null) {
            emitEvent = event.createChild(eventName);
        }else {
            //emitEvent = WorkEventFactory.createWithOrigin(eventName,event.origin());
            emitEvent.setThrowable(error);
        }

        plan.emit(emitEvent, delayTime);

        if(eventName.indexOf("error::")==0) {
            final WorkEvent we = event.createChild(eventName);
            we.setFireEventName(WorkFlow.ERROR);
            plan.emit(we,delayTime);

        }
    }

    static void emitLocalEvent(ActivatedPlan plan, WorkEvent event, String[] eventNames, long delayTime) {

        if(eventNames==null) return;

        for (String eventName : eventNames) {
            emitLocalEvent(plan, event, eventName, delayTime, null);
        }
    }

    static void emitLocalEvent(ActivatedPlan plan, WorkEvent event, String[] eventNames, Throwable error) {

        if(eventNames==null) return;

        for (String eventName : eventNames) {
            emitLocalEvent(plan, event, eventName, 0, error);
        }
    }

}
