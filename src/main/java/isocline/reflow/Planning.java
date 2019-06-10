package isocline.reflow;

import java.util.Date;
import java.util.function.Consumer;

public interface Planning {
    String getId();

    void lock(Object lockOwner) throws IllegalAccessException;

    void unlock(Object lockOwner) throws IllegalAccessException;

    void setWorkObject(Work workObject);

    Work getWorkObject();

    Planning startTime(long nextExecuteTime);

    Planning startDelayTime(long waitTime);

    Planning interval(long intervalTime);

    Planning startTime(String isoDateTime) throws java.text.ParseException;

    Planning startTime(Date startDateTime);

    Planning finishTime(String isoDateTime) throws java.text.ParseException;

    Planning finishTime(Date endDateTime);

    Planning finishTimeFromNow(long milliSeconds);

    void raiseLocalEvent(WorkEvent event);

    void raiseLocalEvent(WorkEvent event, long delayTime);

    Planning on(String... eventNames);

    Planning setStrictMode();

    Planning setBetweenStartTimeMode(boolean isBetweenStartTimeMode);

    Planning jitter(long jitter);

    Planning daemon();

    Plan activate();

    Planning describe(PlanDescriptor descriptor);

    Plan activate(Consumer consumer);

    Planning run();

    boolean isActivated();

    void finish();

    FlowProcessor getFlowProcessor();

    Planning executeEventChecker(ExecuteEventChecker checker);

    Throwable getError();

    void setError(Throwable error);
}
