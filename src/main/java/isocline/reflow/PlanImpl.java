/*
 * Copyright 2018 The Isocline Project
 *
 * The Isocline Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package isocline.reflow;

import isocline.reflow.event.EventRepository;
import isocline.reflow.event.SimultaneousEventSet;
import isocline.reflow.event.WorkEventFactory;
import isocline.reflow.flow.func.WorkEventConsumer;
import isocline.reflow.log.XLogger;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


/**
 * Process various settings related to the execution of the job.
 * You can set scheduling related to tasks such as start time, end time, and repeat time.
 *
 * @see isocline.reflow.Work
 */
public class PlanImpl implements Plan, Activity {

    protected static XLogger logger = XLogger.getLogger(Plan.class);


    private static final long UNDEFINED_INTERVAL = -1;

    private static final long PREEMPTIVE_CHECK_MILLITIME = 2;


    private final String uuid;

    private long waitingTime = 0;

    private long nextExecuteTime = 0;


    private boolean isDefinedStartTime = false;

    private long workEndTime = 0;

    private boolean isLock = false;

    private boolean isActivated = false;

    private long intervalTime = UNDEFINED_INTERVAL;

    private long jitter = 0;


    private boolean isStrictMode = false;

    private boolean isBetweenStartTimeMode = true;

    private boolean needWaiting = false;

    private boolean isEventBindding = false;

    private boolean isDaemonMode = false;


    private boolean isFlowableWork = false;

    private boolean isRunnable = false;

    private long intervalTime4Runnable = Work.TERMINATE;

    private long intervalTime4Flow = 0;


    private Work work;

    private WorkSession workSession = null;

    private Object lockOwner = null;

    private FlowProcessor flowProcessor = null;

    private final LinkedList<WorkEvent> eventList = new LinkedList<>();


    private WorkFlow workFlow = null;

    private ExecuteEventChecker executeEventChecker = null;

    private Throwable error = null;

    private Consumer consumer = null;


    private final Map<String, AtomicInteger> eventCheckMap = new Hashtable<>();

    private final static int STATUS_RECEIVE_OK = -1;

    private final static int STATUS_NOBODY_RECEIVE = -2;


    private int maximumEventSkipCount = 0;


    PlanImpl(FlowProcessor flowProcessor, WorkEventConsumer runnable) {
        Work work = (WorkEvent e) -> {
            runnable.accept(e);

            return intervalTime;
        };
        this.flowProcessor = flowProcessor;
        this.work = work;
        this.uuid = UUID.randomUUID().toString();

        this.isRunnable = true;
        this.intervalTime = Work.TERMINATE;
    }


    PlanImpl(FlowProcessor flowProcessor, Runnable runnable) {
        Work work = (WorkEvent e) -> {
            runnable.run();

            return intervalTime;
        };
        this.flowProcessor = flowProcessor;
        this.work = work;
        this.uuid = UUID.randomUUID().toString();

        this.isRunnable = true;
        this.intervalTime = Work.TERMINATE;
    }


    PlanImpl(FlowProcessor flowProcessor, FlowableWork work) {
        this(flowProcessor, (Work) work);

        this.isFlowableWork = true;
    }

    PlanImpl(FlowProcessor flowProcessor, WorkFlow flow) {
        FlowableWork work = workFlow -> {
        };

        this.flowProcessor = flowProcessor;
        this.work = work;
        this.workFlow = flow;


        this.isFlowableWork = true;

        this.uuid = UUID.randomUUID().toString();
    }


    PlanImpl(FlowProcessor flowProcessor, Work work) {
        this.flowProcessor = flowProcessor;
        this.work = work;

        this.uuid = UUID.randomUUID().toString();
    }


    /**
     * Returns a ID of Plan
     *
     * @return a ID of Plan
     */
    @Override
    public String getId() {
        return this.uuid;
    }

    //
    private void checkLocking() throws RuntimeException {
        if (isLock) {
            throw new RuntimeException("Changing settings is prohibited.");
        }

    }


    void adjustWaiting() throws InterruptedException {
        if (this.isStrictMode && needWaiting) {

            synchronized (uuid) {
                for (int i = 0; i < 10000000; i++) {

                    if (nextExecuteTime <= System.currentTimeMillis()) {
                        return;
                    }
                }
            }
        }
    }


    long getPreemptiveMilliTime() {
        if (this.isStrictMode) {
            return PREEMPTIVE_CHECK_MILLITIME;
        } else {
            return 0;
        }

    }

    long checkRemainMilliTime() {
        needWaiting = false;

        if (!isActivated) {
            return 0;
            //throw new RuntimeException("service end");
        }
        if (isOveEndTimeOver()) {
            return Long.MAX_VALUE;
        }


        if (this.waitingTime == 0) {
            if (this.isStrictMode) {
                needWaiting = true;
            }

            return 0;
        } else if (this.eventList.size() > 0) {
            return 0;
        } else if (this.nextExecuteTime > 0) {

            long t1 = this.nextExecuteTime - System.currentTimeMillis();


            if (t1 <= 0) {
                return t1;
            } else if (this.isStrictMode && t1 < this.getPreemptiveMilliTime()) {
                needWaiting = true;
                return t1;
            }

            return t1;

        }


        //return Time.HOUR;
        return Long.MAX_VALUE;
    }


    long getNextExecuteTime() {
        return this.nextExecuteTime;
    }


    boolean isOveEndTimeOver() {
        if (this.workEndTime > 0) {
            if (System.currentTimeMillis() >= this.workEndTime) {

                return true;
            }
        }


        return false;
    }


    // public method


    @Override
    public void lock(Object lockOwner) throws IllegalAccessException {
        if (this.lockOwner != null) {
            throw new IllegalAccessException("Already locking");
        }
        this.lockOwner = lockOwner;
        this.isLock = true;
    }


    /**
     * Unlock
     *
     * @param lockOwner The object that performed the lock
     * @throws IllegalAccessException If the Lock object and the input object are not the same
     */
    @Override
    public void unlock(Object lockOwner) throws IllegalAccessException {
        if (lockOwner == this.lockOwner) {
            this.isLock = false;
            this.lockOwner = null;
        } else {
            throw new IllegalAccessException("not lock owner");
        }
    }


    /**
     * Sets a {@link Work} implement object.
     *
     * @param workObject an instance of WorkObject
     */

    public void setWorkObject(Work workObject) {
        this.work = workObject;
    }

    /**
     * @return a instance of Work
     */

    public Work getWorkObject() {
        return this.work;
    }


    @Override
    public Plan startTime(long nextExecuteTime) {

        this.isDefinedStartTime = true;
        return setStartTime(nextExecuteTime);
    }

    /**
     * Sets a start time for this schedule
     *
     * @param nextExecuteTime the number of milliseconds since January 1, 1970, 00:00:00 GMT for the date and time specified by the arguments.
     * @return an instance of Plan
     */
    private Plan setStartTime(long nextExecuteTime) {


        /*
        if (waitingTime == 0) {
            waitingTime = 1;
        }
        */


        this.nextExecuteTime = nextExecuteTime;

        return this;
    }

    @Override
    public Plan initialDelay(long waitTime) {
        checkLocking();
        this.waitingTime = waitTime;

        return this;
    }

    /**
     * Adjust a delay time
     *
     * @param waitTime a milliseconds for timeout
     * @return an instance of Plan
     */
    Plan adjustDelayTime(long waitTime) {


        this.waitingTime = waitTime;

        if (waitTime < 0) {
            this.nextExecuteTime = UNDEFINED_INTERVAL;


        } else {

            if (this.isBetweenStartTimeMode && this.nextExecuteTime > 0) {

                long tmp = System.currentTimeMillis() - nextExecuteTime;


                if (tmp > 0) {
                    long x = (long) Math.ceil((double) tmp / (double) waitTime);

                    setStartTime(this.nextExecuteTime + waitTime * (x));

                    return this;
                } else if (tmp == 0) {
                    setStartTime(this.nextExecuteTime + waitTime);
                    return this;
                }


            }

            long crntTime = System.currentTimeMillis();
            long adjCrntTime = crntTime;
            if (this.isStrictMode) {
                for (int i = 0; i < 5; i++) {
                    long adjTime = (crntTime - (crntTime % 1000)) + this.jitter + i * 1000;
                    long nextTime = adjTime + waitTime;

                    if ((crntTime + this.jitter) < nextTime) {
                        adjCrntTime = adjTime;
                        break;
                    }
                }
            }


            long chkTime = adjCrntTime + waitTime;
            if (this.nextExecuteTime < chkTime) {
                setStartTime(chkTime);

            }


        }


        return this;
    }


    /**
     * Returns an interval time
     *
     * @return a milliseconds time for interval
     */
    @Override
    public long getIntervalTime() {
        return this.intervalTime;
    }


    /**
     * Sets an interval time
     *
     * @param intervalTime a milliseconds time for interval
     * @return an instance of Plan
     */
    @Override
    public Plan interval(long intervalTime) {

        checkLocking();


        if (this.isFlowableWork) {
            this.intervalTime4Flow = intervalTime;
        } else {
            this.intervalTime = intervalTime;
        }

        return this;
    }


    @Override
    public Plan interval(long initialDelay, long intervalTime) {
        this.initialDelay(initialDelay);

        return this.interval(intervalTime);
    }



    /**
     * @param intervalTime
     * @return
     */
    Plan adjustRepeatInterval(long intervalTime) {


        this.intervalTime = intervalTime;

        return initialDelay(intervalTime);

    }


    /**
     * ISO 8601 Data elements and interchange formats (https://en.wikipedia.org/wiki/ISO_8601)
     *
     * @param isoDateTime this date-time as a String, such as 2019-06-16T10:15:30Z or 2019-06-16T10:15:30+01:00[Europe/Paris].
     * @return an instance of Plan
     * @throws java.text.ParseException if date time format is not valid.
     */
    @Override
    public Plan startTime(String isoDateTime) throws java.text.ParseException {

        this.isDefinedStartTime = true;

        return startTime(Time.toDate(isoDateTime));
    }


    /**
     * Sets a start date time
     *
     * @param startDateTime Date of start
     * @return an instance of Plan
     */
    @Override
    public Plan startTime(Date startDateTime) {

        this.isDefinedStartTime = true;

        this.setStartTime(startDateTime.getTime());
        return this;
    }


    /**
     * ISO 8601 Data elements and interchange formats (https://en.wikipedia.org/wiki/ISO_8601)
     *
     * @param isoDateTime this date-time as a String, such as 2019-06-16T10:15:30Z or 2019-06-16T10:15:30+01:00[Europe/Paris].
     * @return an instance of Plan
     * @throws java.text.ParseException if date time format is not valid.
     */
    @Override
    public Plan finishTime(String isoDateTime) throws java.text.ParseException {

        return finishTime(Time.toDate(isoDateTime));
    }


    /**
     * Sets inactive date time
     *
     * @param endDateTime Date of end
     * @return an instance of Plan
     */
    @Override
    public Plan finishTime(Date endDateTime) {

        this.workEndTime = endDateTime.getTime();
        return this;
    }

    /**
     * Sets a inactive time from now
     *
     * @param milliSeconds milliseconds
     * @return an instance of Plan
     */
    @Override
    public Plan finishTimeFromNow(long milliSeconds) {
        this.workEndTime = System.currentTimeMillis() + milliSeconds;
        return this;
    }

    @Override
    public Plan finishTimeFromStart(long milliSeconds) {
        if (this.nextExecuteTime > 0) {
            this.workEndTime = this.nextExecuteTime + milliSeconds;
        } else {
            this.workEndTime = System.currentTimeMillis() + milliSeconds;
        }

        return this;
    }


    @Override
    public Activity finish(String isoDateTime) throws ParseException {
        return (Activity) finishTime(isoDateTime);
    }

    @Override
    public Activity finish(Date endDateTime) {
        return (Activity) finishTime(endDateTime);
    }

    @Override
    public Activity finishFromNow(long milliSeconds) {
        return (Activity) finishTimeFromNow(milliSeconds);
    }

    /**
     * @param className name of class
     * @return an instance of Plan
     * @throws ClassNotFoundException if the class cannot be located
     * @throws InstantiationException if this Class represents an abstract class, an interface, an array class, a primitive type, or void; or if the class has no nullary constructor; or if the instantiation fails for some other reason.
     * @throws IllegalAccessException if the class or its nullary constructor is not accessible.
     */
    public Plan workSession(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        checkLocking();
        this.workSession = (WorkSession) Class.forName(className).newInstance();
        return this;
    }

    /**
     * Sets a {@link WorkSession}
     *
     * @param workSession an instance of WorkSession
     * @return an instance of Plan
     */
    public Plan workSession(WorkSession workSession) {
        checkLocking();

        this.workSession = workSession;
        return this;
    }


    /**
     * Returns a {@link WorkSession}
     *
     * @return an instance of WorkSession
     */
    public WorkSession getWorkSession() {

        if (this.workSession == null) {
            this.workSession = new BasicWorkSession();

        }

        return this.workSession;
    }


    public WorkFlow getWorkFlow() {
        return this.workFlow;
    }


    ////////////////




    public void setMaximumEventSkipCount(int maximumEventSkipCount) {
        if (maximumEventSkipCount < 0) {
            throw new IllegalArgumentException("maximumEventSkipCount is too small");
        }
        this.maximumEventSkipCount = maximumEventSkipCount;
    }

    @Override
    public Activity emit(WorkEvent event) {

        if (event == null) {
            throw new FlowProcessException("Event is null");
        }

        String fireEventName = event.getFireEventName();

        if (maximumEventSkipCount > 0) {

            AtomicInteger counter = eventCheckMap.computeIfAbsent(fireEventName, k -> new AtomicInteger(0));
            int chkCount = counter.get();


            if (chkCount > maximumEventSkipCount) {
                counter.set(STATUS_NOBODY_RECEIVE);
            } else {
                switch (chkCount) {
                    case STATUS_NOBODY_RECEIVE:
                        return this;

                    case STATUS_RECEIVE_OK:
                        break;

                    default:
                        counter.addAndGet(1);

                }

            }
        }

        //System.out.println(" [0] >> " + event.getEventName() + " : " + event.getFireEventName());

        this.flowProcessor.addWorkSchedule(this, event);

        return this;

    }

    void checkEvent(WorkEvent event) {

        String fireEventName = event.getFireEventName();

        if (fireEventName == null) return;

        AtomicInteger counter = eventCheckMap.get(fireEventName);
        if (counter != null && counter.get() != STATUS_RECEIVE_OK) {
            counter.set(STATUS_RECEIVE_OK);
        }

    }


    @Override
    public Activity emit(WorkEvent event, long delayTime) {

        if (event == null) {
            throw new FlowProcessException("Event is null");
        }

        if (delayTime > 0) {
            //this.flowProcessor.workChecker
            //System.out.println(" [1] >> " + event.getEventName() + " : " + event.getFireEventName() + " delayTime:" + delayTime + "\n");

            this.flowProcessor.addWorkSchedule(this, event, delayTime);

        } else {
            emit(event);
        }

        return this;

    }


    @Override
    public Plan on(Object... eventNames) {
        checkLocking();

        for (Object eventName1 : eventNames) {
            String eventName = eventName1.toString();
            String[] subEventNames = eventRepository.setBindEventNames(eventName);
            for (String subEventName : subEventNames) {
                this.flowProcessor.bindEvent(this, subEventName);
            }
        }
        this.isEventBindding = true;

        return this;
    }


    @Override
    public Plan strictMode() {
        checkLocking();
        this.isStrictMode = true;
        return this;
    }


    public boolean isStrictMode() {
        return this.isStrictMode;
    }


    @Override
    public Plan setBetweenStartTimeMode(boolean isBetweenStartTimeMode) {
        checkLocking();
        this.isBetweenStartTimeMode = isBetweenStartTimeMode;
        return this;
    }

    @Override
    public Plan jitter(long jitter) {
        checkLocking();
        this.jitter = jitter;
        return this;
    }

    @Override
    public Plan daemonMode() {
        checkLocking();
        this.initialDelay(Work.WAIT);

        this.intervalTime = Work.WAIT;
        this.isDaemonMode = true;
        return this;
    }


    @Override
    public boolean isDaemonMode() {

        return intervalTime4Flow > 0 || isEventBindding || this.isDaemonMode;

    }

    @Override
    public Activity activate() {
        return activate(null);
    }


    @Override
    public Plan describe(PlanDescriptor descriptor) {
        descriptor.build(this);
        return this;
    }

    void clearForLooping() {
        this.originEvent = null;
    }

    /**
     * Actives a {@link Plan}
     *
     * @param consumer Consumer
     * @return an instance of Plan
     */
    @SuppressWarnings("unchecked")
    @Override
    public Activity activate(Consumer consumer) {
        if (isActivated) {
            throw new RuntimeException("Already activate!");
        }
        this.consumer = consumer;
        this.isActivated = true;


        if (this.isFlowableWork) {

            if (this.work instanceof FlowableWork) {
                FlowableWork fw = (FlowableWork) this.work;

                if (this.workFlow == null) {
                    this.workFlow = WorkFlow.create();

                    WorkFlow wf = this.workFlow
                            .next(fw::initialize);


                    fw.defineWorkFlow(wf);

                }

                if (!this.isDaemonMode && !workFlow.isSetFinish()) {

                    workFlow.fireEvent(WorkFlow.FINISH, 0);
                    workFlow.wait(WorkFlow.FINISH).end();
                }

            }

        }

        if (this.isStrictMode && !this.isDefinedStartTime) {

            long startTime = Time.nextSecond(900);
            this.setStartTime(startTime + this.waitingTime);
        } else if (this.waitingTime > 0) {
            this.adjustDelayTime(this.waitingTime);
        }


        if (this.waitingTime != Work.WAIT) {
            if (this.isStrictMode || this.isDefinedStartTime || this.waitingTime > 1 || isEventBindding) {
                this.flowProcessor.addWorkSchedule(this, false);
            } else {
                this.flowProcessor.addWorkSchedule(this, true);
            }

        }

        this.flowProcessor.managedWorkCount.incrementAndGet();

        return this;
    }


    /**
     * @return
     */
    @Override
    public Activity run() {
        Activity schedule = activate();

        schedule.block();


        Throwable error = schedule.getError();
        if (error != null) {
            throw new RuntimeException(error);
        }

        return schedule;
    }


    /**
     * when activated
     *
     * @return True if Plan is activated
     */
    @Override
    public boolean isActivated() {
        return isActivated;
    }


    public long getIntervalTime4Flow() {

        return this.intervalTime4Flow;

    }

    /**
     * inactive job
     */
    @Override
    synchronized public void inactive() {


        if (this.isActivated) {
            this.isActivated = false;
            this.flowProcessor.managedWorkCount.decrementAndGet();

            if (this.consumer != null) {
                WorkEvent originEvent = this.getOriginWorkEvent();
                Object result = WorkHelper.Get(originEvent);
                consumer.accept(result);
            }
            notifyAll();

            //logger.debug("Plan is finished");
        }
    }


    synchronized public Activity block(long timeout) {

        try {

            if (this.isActivated) {
                wait(timeout);
            }

        } catch (InterruptedException ignored) {

        }
        return this;
    }


    synchronized public Activity block() {
        try {
            if (this.isActivated) {
                wait();
            }
        } catch (InterruptedException ignored) {

        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlanImpl plan = (PlanImpl) o;

        return uuid.equals(plan.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }


    @Override
    public FlowProcessor getFlowProcessor() {
        return flowProcessor;
    }


    @Override
    public Plan eventChecker(ExecuteEventChecker checker) {
        this.executeEventChecker = checker;
        return this;
    }

    boolean isExecuteEnable(long time) {
        return this.executeEventChecker == null || this.executeEventChecker.check(time);

    }

    @Override
    public Throwable getError() {
        return error;
    }

    @Override
    public void setError(Throwable error) {
        this.error = error;
    }


///////////////////////////////////////////////////////////////////


    private WorkEvent originEvent = null;

    private WorkEvent getOriginWorkEvent() {
        return this.originEvent;
    }

    WorkEvent getOriginWorkEvent(WorkEvent inputWorkEvent) {

        WorkEvent event;

        if (inputWorkEvent == null) {
            //System.err.println("== 0 ===============");
            if (originEvent == null) {
                originEvent = WorkEventFactory.createOrigin();
                originEvent.setActivity(this);
                //System.err.println("== 1");
            } else {
                //System.err.println("== 2 "+originEvent.getEventName());
            }
            //event = originEvent;

            event = WorkEventFactory.createWithOrigin(null, originEvent);
            event.setActivity(originEvent.getActivity());

        } else {
            if (originEvent == null) {
                originEvent = inputWorkEvent;
                //System.err.println("== 3 "+inputWorkEvent.getEventName());
            } else {
                //System.err.println("== 4 "+inputWorkEvent.getEventName());
            }

            event = inputWorkEvent;
            event.setActivity(this);
        }

        return event;


    }


    private final EventRepository eventRepository = new EventRepository();


    /**
     * @param eventName name of event
     * @return name of event
     */
    String getDeliverableEventName(String eventName, WorkEvent event) {


        SimultaneousEventSet simultaneousEventSet = eventRepository.getSimultaneousEventSet(eventName);


        if (simultaneousEventSet == null) {
            return eventName;
        }

        WorkEvent originEvent = event.origin();


        if (simultaneousEventSet.isRaiseEventReady(event, eventName)) {

            return simultaneousEventSet.getEventSetName();
        }


        return null;

    }


    ////////////////////////////


    ExecuteContext enterQueue(boolean isUserEvent, WorkEvent workEvent) {


        return new ExecuteContext(this, isUserEvent, workEvent);
    }


    ExecuteContext enterQueue(boolean isUserEvent) {


        return new ExecuteContext(this, isUserEvent, null);
    }


    /**
     * ExecuteContext for Queue
     */
    static class ExecuteContext {


        private final PlanImpl plan;

        private boolean isUserEvent = false;

        private final WorkEvent workEvent;


        ExecuteContext(PlanImpl plan, boolean isUserEvent, WorkEvent event) {

            this.plan = plan;
            this.isUserEvent = isUserEvent;

            this.workEvent = event;

            /*
            if(event!=null) {
                event.setActivity(plan);

            }
            */
        }


        boolean isExecuteImmediately() {
            if (isUserEvent) {
                this.isUserEvent = false;

                return true;
            }

            return false;
        }

        PlanImpl getPlan() {

            //if (this.contextId == this.plan.contextCheckId)
            {
                return plan;
            }

            //return null;

        }

        WorkEvent getWorkEvent() {
            return this.workEvent;
        }


    }

}