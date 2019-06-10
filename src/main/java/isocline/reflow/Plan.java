package isocline.reflow;

import java.util.Date;
import java.util.function.Consumer;

public interface Plan {


    String getId();

    void lock(Object lockOwner) throws IllegalAccessException;

    void unlock(Object lockOwner) throws IllegalAccessException;


    Plan startTime(long nextExecuteTime);

    Plan startDelayTime(long waitTime);

    Plan interval(long intervalTime);

    Plan startTime(String isoDateTime) throws java.text.ParseException;

    Plan startTime(Date startDateTime);

    Plan finishTime(String isoDateTime) throws java.text.ParseException;

    Plan finishTime(Date endDateTime);

    Plan finishTimeFromNow(long milliSeconds);

    void emit(WorkEvent event);

    void emit(WorkEvent event, long delayTime);

    Plan on(String... eventNames);


    Plan setBetweenStartTimeMode(boolean isBetweenStartTimeMode);

    Plan jitter(long jitter);


    Plan strictMode();

    Plan daemonMode();


    FlowProcessor getFlowProcessor();

    Plan eventChecker(ExecuteEventChecker checker);


    Plan describe(PlanDescriptor descriptor);

    Plan activate();

    Plan activate(Consumer consumer);

    Plan run();

    boolean isActivated();




    WorkFlow getWorkFlow();





    void inactive();

    Plan block();

    Plan block(long timeout);

    Throwable getError();

    void setError(Throwable error);
}
