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

import isocline.reflow.event.WorkEventFactory;
import isocline.reflow.flow.WorkInfo;
import isocline.reflow.log.XLogger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Base class for Plan management and thread management
 *
 * @author Richard D. Kim
 * @see isocline.reflow.Work
 * @see isocline.reflow.FlowableWork
 * @see Plan
 */
public class FlowProcessor extends ThreadGroup {

    protected static XLogger logger = XLogger.getLogger(FlowProcessor.class);


    private String name;

    private boolean isWorking = false;

    private int checkpointWorkQueueSize = 500;


    private Configuration configuration;

    private List<ThreadWorker> threadWorkers = new ArrayList<>();

    private AtomicInteger currentThreadWorkerCount = new AtomicInteger(0);

    private BlockingQueue<PlanImpl.ExecuteContext> workQueue;

    private WorkChecker workChecker;

    private Map<String, WorkScheduleList> eventMap = new ConcurrentHashMap<>();


    AtomicInteger managedWorkCount = new AtomicInteger(0);


    private static FlowProcessor defaultFlowProcessor;

    private static Map<String, FlowProcessor> processorMap = new HashMap<>();


    public static FlowProcessor core() {


        if (defaultFlowProcessor == null || !defaultFlowProcessor.isWorking()) {
            defaultFlowProcessor = new FlowProcessor("default", getDefaultConfiguration());
        }

        return defaultFlowProcessor;
    }

    private static Configuration getDefaultConfiguration() {
        String processorType = System.getProperty("isocline.Reflow.processor.type");

        if ("performance".equals(processorType)) {
            return Configuration.PERFORMANCE;
        } else if ("echo".equals(processorType)) {
            return Configuration.ECHO;
        } else if ("hyper".equals(processorType)) {
            return Configuration.HYPER;
        }

        return Configuration.NOMAL;
    }


    /**
     * Create a FlowProcessor object which provice services for Plan
     *
     * @param name   ClockerWorker name
     * @param config configuration for FlowProcessor
     */
    FlowProcessor(String name, Configuration config) {
        super("FlowProcessor");

        this.name = "FlowProcessor[" + name + "]";

        this.configuration = config.lock();
        this.checkpointWorkQueueSize = config.getMaxWorkQueueSize() / 1000;
        if (this.checkpointWorkQueueSize < 500) {
            this.checkpointWorkQueueSize = 500;
        }

        this.workQueue = new LinkedBlockingQueue<>(this.configuration.getMaxWorkQueueSize());

        init(true);


        logger.info(this.name + " initialized");
    }


    private void init(boolean isWorking) {

        this.isWorking = isWorking;

        workChecker = new WorkChecker(this);
        workChecker.start();

        addThreadWorker(this.configuration.getInitThreadWorkerSize());

    }

    /**
     * Register the task to be bound to the input events.
     *
     * @param work       an instance of Work
     * @param eventNames an event names
     * @return an new instance of Plan
     */
    public Plan reflow(Work work, String... eventNames) {
        Plan Plan = new PlanImpl(this, work);
        Plan.daemonMode();
        Plan.on(eventNames);


        return Plan;
    }


    public Plan reflow(FlowableWork<?> workFlow) {

        Plan Plan = new PlanImpl(this, workFlow);


        return Plan;
    }


    /**
     * Create a Plan instance.
     *
     * @param work Work implement class object
     * @return new instance of Plan
     */
    public Plan reflow(Work work) {
        return reflow(null, work);
    }

    public Plan reflow(Runnable runnable) {
        return reflow(null, runnable);
    }


    public Plan reflow(PlanDescriptor config, Work work) {
        Plan plan = new PlanImpl(this, work);
        if (config != null) {
            plan.describe(config);
        }

        return plan;
    }

    public Plan reflow(PlanDescriptor config, Runnable runnable) {

        Plan plan = new PlanImpl(this, runnable);
        if (config != null) {
            plan.describe(config);
        }

        return plan;
    }


    /**
     * Create a Plan instance by work class
     *
     * @param workClass class of implement for Work
     * @return new instance of Plan
     * @throws InstantiationException InstantiationException
     * @throws IllegalAccessException IllegalAccessException
     */
    public Plan reflow(Class workClass) throws InstantiationException, IllegalAccessException {
        return reflow((Work) workClass.newInstance());
    }


    /**
     * Create a Plan instance by work class
     *
     * @param descriptor an description for scheduling
     * @param workClass  class of implement for Work
     * @return new instance of Plan
     * @throws InstantiationException InstantiationException
     * @throws IllegalAccessException IllegalAccessException
     */
    public Plan reflow(PlanDescriptor descriptor, Class workClass) throws InstantiationException, IllegalAccessException {
        return reflow(descriptor, (Work) workClass.newInstance());
    }


    /**
     * Create a Plan instance by work classname
     *
     * @param className classname of implement for Work
     * @return new instance of Plan
     * @throws ClassNotFoundException if the class cannot be located
     * @throws InstantiationException if this Class represents an abstract class, an interface, an array class, a primitive type, or void; or if the class has no nullary constructor; or if the instantiation fails for some other reason.
     * @throws IllegalAccessException if the class or its nullary constructor is not accessible.
     */
    public Plan reflow(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return new PlanImpl(this, (Work) Class.forName(className).newInstance());
    }


    public Activity execute(Work work) {
        return execute(work, 0);
    }

    public Activity execute(Work work, long startDelayTime) {
        Plan plan = new PlanImpl(this, work);
        if (startDelayTime > 0) {
            plan.startDelayTime(startDelayTime);
        }

        return plan.activate().block();


    }


    /**
     * Returns a count of managed Work implement instance.
     *
     * @return count of managed Work implement instance.
     */
    public int getManagedWorkCount() {
        return this.managedWorkCount.get();
    }


    /**
     * Returns a count of running Thread for processing Work object.
     *
     * @return a current count of thread worker
     */
    public int getCurrentThreadWorkerCount() {
        return this.currentThreadWorkerCount.get();
    }

    /**
     * Returns a current queue size for processing Work object.
     *
     * @return a current size of work queue
     */
    public int getWorkQueueSize() {
        return this.workQueue.size();
    }


    /**
     * Wait until completion of work.when the time is up, stop waiting
     *
     * @param timeout milli seconds for timeout
     */
    public void waitingJob(long timeout) {

        long tt1 = System.currentTimeMillis();


        long t1 = System.currentTimeMillis() + timeout;

        try {
            long gap = t1 - System.currentTimeMillis();
            Thread.sleep(500);
            while (gap > 1) {
                gap = t1 - System.currentTimeMillis();
                if (this.getManagedWorkCount() > 0) {
                    Thread.sleep(100);

                } else {
                    if (this.getManagedWorkCount() == 0
                            && workQueue.size() == 0) {
                        break;
                    }
                }
            }
        } catch (Exception e) {

        }


        for (ThreadWorker t : this.threadWorkers) {
            t.interrupt();
        }


        long tt2 = System.currentTimeMillis();

        long gap = (tt2 - tt1);
        if (gap > 0) {
            logger.warn(this.name + " wait time(milliseconds) for async job : " + gap);
        }
    }


    public void awaitShutdown() {

        long t1 = System.currentTimeMillis();

        while (this.getManagedWorkCount() > 0) {

            waiting(200);

            long t2 = System.currentTimeMillis();

            long gap = t2 - t1;
            if (gap > 1000 * 60 * 3) {
                logger.debug("running work count:" + this.getManagedWorkCount());
                t1 = System.currentTimeMillis();

            }

        }


        shutdown();

    }


    /**
     * Shutdown all process for these services. but wait for process complete until maximum 10 seconds
     */
    public void shutdown() {


        int count = 0;
        isWorking = false;
        while (this.getManagedWorkCount() > 0) {

            waiting(100);
            count++;

            // total 1 second
            if (count > 10) {
                break;
            }
        }


        try {
            super.destroy();
        } catch (IllegalThreadStateException ite) {

        } finally {
            logger.info(this.name + " shutdown");
        }
    }


    /**
     * Shutdown all process for these services. but wait for process complete until timeout
     *
     * @param timeout a milliseconds for timeout
     */
    public void shutdown(long timeout) {

        if (timeout > 0) {

            waitingJob(timeout);
        }

        shutdown();
    }


    boolean addWorkSchedule(PlanImpl plan) {

        return addWorkSchedule(plan, false);
    }


    boolean addWorkSchedule(PlanImpl plan, boolean isUserEvent) {

        boolean result = this.workQueue.offer(plan.enterQueue(isUserEvent));
        if (result) {


            int sz = this.workQueue.size();
            if (sz > checkpointWorkQueueSize) {
                this.checkPoolThread();
            }
        } else {
            this.checkPoolThread();
        }

        return result;
    }

    boolean addWorkSchedule(PlanImpl plan, WorkEvent workEvent) {

        boolean result = this.workQueue.offer(plan.enterQueue(true, workEvent));
        if (result) {

            int sz = this.workQueue.size();
            if (sz > checkpointWorkQueueSize) {
                this.checkPoolThread();
            }
        } else {
            this.checkPoolThread();
        }

        return result;
    }


    boolean addWorkSchedule(PlanImpl plan, WorkEvent workEvent, long delayTime) {

        workEvent.setFireTime(System.currentTimeMillis() + delayTime);

        this.workChecker.addWorkStatusWrapper(plan, workEvent);

        return true;
    }


    private void waiting(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {

        }
    }


    private synchronized boolean addThreadWorker(int addSize) {

        boolean result = false;

        for (int s = 0; s < addSize; s++) {

            if (currentThreadWorkerCount.get() < this.configuration.getMaxThreadWorkerSize()) {

                ThreadWorker mon = new ThreadWorker(this, this.configuration.getThreadPriority());
                threadWorkers.add(mon);
                mon.setDaemon(true);
                mon.start();

                result = true;

                currentThreadWorkerCount.addAndGet(1);
            }
        }

        if (result) {
            logger.info("new ThreadWorker (+" + addSize + ") current:" + currentThreadWorkerCount.get());
        }
        return result;

    }


    /**
     * @param t
     * @return
     */
    private synchronized boolean removeThreadWorker(ThreadWorker t) {

        currentThreadWorkerCount.addAndGet(-1);
        return threadWorkers.remove(t);

    }

    /**
     * @return
     */
    private synchronized boolean removeThreadWorker() {

        if (currentThreadWorkerCount.get() > this.configuration.getInitThreadWorkerSize()) {
            try {
                ThreadWorker mon = threadWorkers.get(0);
                mon.stopWorking();
                return true;
            } catch (Exception e) {

            }
        }
        return false;
    }


    /**
     *
     */
    private synchronized void checkPoolThread() {

        logger.debug("checkPoolThread");

        for (ThreadWorker t : this.threadWorkers) {
            if (t.isDelayExecute()) {
                t.stopWorking();
            }
        }
    }


    private WorkScheduleList getWorkScheduleList(String eventName, boolean isCreate) {
        WorkScheduleList workScheduleList = eventMap.get(eventName);
        if (isCreate && workScheduleList == null) {
            workScheduleList = eventMap.computeIfAbsent(eventName, k -> new WorkScheduleList());
        }
        return workScheduleList;
    }

    void bindEvent(Plan Plan, String eventName) {


        WorkScheduleList workScheduleMap = getWorkScheduleList(eventName, true);

        workScheduleMap.add(Plan);


    }


    void bindEvent(Plan Plan, String... eventNames) {

        for (String eventName : eventNames) {
            bindEvent(Plan, eventName);
        }


    }


    void unbindEvent(Plan Plan, String... eventNames) {

        for (String eventName : eventNames) {
            WorkScheduleList workScheduleMap = getWorkScheduleList(eventName, false);
            if (workScheduleMap == null) {
                return;
            }

            workScheduleMap.remove(Plan);
        }
    }

    public FlowProcessor emit(WorkEvent event) {
        String eventName = event.getEventName();
        if (eventName != null && eventName.length() > 0) {
            emit(eventName, event);
        }

        return this;
    }
    public FlowProcessor emit(String eventName,   WorkEvent event) {
        return emit(eventName, null, event);

    }

    public FlowProcessor emit(String eventName, String internalTargetEventName, WorkEvent event) {

        WorkScheduleList workScheduleList = getWorkScheduleList(eventName, false);

        WorkEvent workEvent;
        if (event == null) {
            workEvent = WorkEventFactory.createOrigin();
        } else {
            workEvent = WorkEventFactory.createWithOrigin(eventName, event);
        }


        // for FlowableWork
        //workEvent.setFireEventName( WorkFlow.START );

        if (workScheduleList != null) {

            PlanImpl[] array = workScheduleList.getPlanArray();
            for (PlanImpl schedule : array) {

                String newEventName = schedule.getDeliverableEventName(eventName, event);

                if (newEventName != null) {
                    WorkEvent newWorkEvent = workEvent;
                    if (!newEventName.equals(eventName)) {
                        /*
                        newWorkEvent = WorkEventFactory.createChild(newEventName);
                        workEvent.copyTo(newWorkEvent);
                        */
                        newWorkEvent = workEvent.createChild(newEventName);

                    }
                    //schedule.emit(newWorkEvent);

                    if(internalTargetEventName!=null) {
                        newWorkEvent.setFireEventName(internalTargetEventName);
                    }

                    this.workQueue.offer(schedule.enterQueue(true, newWorkEvent));

                }


            }
        }

        return this;

    }

    public boolean isWorking() {
        return this.isWorking;
    }


    /****************************************
     *
     * Sub class for Plan information
     *
     ****************************************/
    static final class WorkScheduleList extends HashSet<Plan> {

        private PlanImpl[] array = null;


        private void setArray() {
            array = this.toArray(new PlanImpl[this.size()]);
        }

        PlanImpl[] getPlanArray() {
            return this.array;
        }


        @Override
        public boolean add(Plan Plan) {
            boolean result;
            synchronized (this) {
                result = super.add(Plan);
                this.setArray();
            }
            return result;
        }

        @Override
        public boolean remove(Object o) {
            boolean result;
            synchronized (this) {
                result = super.remove(o);
                this.setArray();
            }
            return result;
        }

        @Override
        public void clear() {

            synchronized (this) {
                super.clear();
                this.setArray();
            }

        }
    }

    /****************************************
     *
     * Thread for Worker
     *
     ****************************************/
    static final class ThreadWorker extends Thread {

        private FlowProcessor flowProcessor;


        private int timeoutCount = 0;

        private static int stoplessCount = 0;

        private long lastWorkTime = 0;

        private boolean isThreadRunning = false;

        private String uuid;

        private static int totalCount = 0;

        private int sequence = 0;

        private int maxWaitTime = 2000;

        private static AtomicInteger runningCounter = new AtomicInteger(0);

        public ThreadWorker(FlowProcessor parent, int threadPriority) {
            super(parent, "Clockwork:ThreadWorker-" + parent.currentThreadWorkerCount);
            this.flowProcessor = parent;
            this.setPriority(threadPriority);

            uuid = UUID.randomUUID().toString();

            totalCount++;
            sequence = totalCount;

            maxWaitTime = 2000 + sequence * 100;

        }


        private boolean isWorking() {
            if (!this.flowProcessor.isWorking) {
                return false;
            } else if (!isThreadRunning) {
                return false;
            }

            return true;
        }


        private boolean check(PlanImpl plan, long time) throws InterruptedException, FlowProcessException {

            if (!plan.isActivated()) {
                //return false;
                throw new FlowProcessException("inactive");
            }

            if (plan.isOveEndTimeOver()) {
                throw new InterruptedException("time out");
            }

            return plan.isExecuteEnable(time);
        }


        public boolean isDelayExecute() {

            long executeTimeout = flowProcessor.configuration.getExecuteTimeout();

            if (this.lastWorkTime < 1 || executeTimeout < 0) {
                return false;
            }

            long t1 = System.currentTimeMillis() - this.lastWorkTime;

            return t1 > executeTimeout;

        }


        private final static short RUNNING = 0;
        private final static short TERMINATE_BY_USER = 1;
        private final static short TERMINATE_BY_TIMEOVER = 2;
        private final static short TERMINATE_BY_ERROR = 3;
        private final static short TERMINATE_BY_FLOW = 4;


        private final static short ENTER_MONITOR_QUEUE = 11;
        private final static short ENTER_EXEC_QUEUE = 12;
        private final static short ENTER_EXEC_QUEUE2 = 13;


        private void adjustThread(int runningCount) {
            if (timeoutCount > 10) {
                this.flowProcessor.removeThreadWorker();
                timeoutCount = 0;
            } else if (runningCount > 3 * (this.flowProcessor.getCurrentThreadWorkerCount() / 4)) {
                stoplessCount = 0;
                this.flowProcessor.addThreadWorker(6);
            }
        }


        private int count = 0;

        private boolean checkExecuteContext(PlanImpl.ExecuteContext ctx) throws InterruptedException {
            if (ctx == null) {
                return false;
            } else if (count == 5000) {
                Thread.sleep(0, 1);
            } else if (count == 10000) {
                Thread.sleep(10);
                count = 0;
            } else {
                count++;
            }

            return true;
        }


        private long checkNextIntervalTime(long nextIntervalTime, PlanImpl plan) {

            long intervalTime = nextIntervalTime;

            if (nextIntervalTime == Work.LOOP) {
                intervalTime = Time.SECOND;

            } else if (nextIntervalTime == Work.WAIT) {

                long configIntervalTime = plan.getIntervalTime();

                if (configIntervalTime > 0) {
                    intervalTime = configIntervalTime;
                }
            }

            if (intervalTime > 0 || intervalTime == Work.WAIT) {
                plan.adjustDelayTime(intervalTime);
            }

            return intervalTime;
        }

        /**
         *
         */
        public void run() {

            isThreadRunning = true;


            while (isWorking()) {

                PlanImpl plan = null;

                short stateMode = RUNNING;

                WorkEvent workEvent = null;

                try {

                    final PlanImpl.ExecuteContext ctx = this.flowProcessor.workQueue.poll(maxWaitTime,
                            TimeUnit.MILLISECONDS);

                    if (!checkExecuteContext(ctx)) {
                        continue;
                    }

                    plan = ctx.getPlan();
                    if (plan == null) continue;

                    timeoutCount = 0;

                    Work workObject = plan.getWorkObject();

                    this.lastWorkTime = System.currentTimeMillis();

                    if (check(plan, this.lastWorkTime)) {

                        long remainTime = plan.checkRemainMilliTime();

                        //logger.error("** "+remainTime + " "+plan.getPreemptiveMilliTime());
                        if (remainTime < 1 || ctx.isExecuteImmediately() || remainTime < plan.getPreemptiveMilliTime()) {

                            stoplessCount++;

                            workEvent = plan.getOriginWorkEvent(ctx.getWorkEvent());


                            //logger.error("event==="+workEvent);

                            plan.adjustWaiting();

                            long nextIntervalTime;

                            try {
                                runningCounter.addAndGet(1);

                                nextIntervalTime = workObject.execute(workEvent);

                                int loopCount = 0;
                                while (nextIntervalTime == Work.LOOP && loopCount < 1000) {
                                    nextIntervalTime = workObject.execute(workEvent);
                                    loopCount++;
                                }
                            } finally {
                                runningCounter.addAndGet(-1);
                            }

                            nextIntervalTime = checkNextIntervalTime(nextIntervalTime, plan);

                            if (nextIntervalTime == Work.TERMINATE) {
                                stateMode = TERMINATE_BY_USER;

                            } else if (nextIntervalTime > this.flowProcessor.configuration.getThresholdWaitTimeToReady()) {
                                stateMode = ENTER_MONITOR_QUEUE;

                            } else if (nextIntervalTime > 0) {
                                stateMode = ENTER_EXEC_QUEUE;
                            }


                        } else if (!plan.isOveEndTimeOver()) {
                            timeoutCount++;
                            stoplessCount = 0;

                            if (remainTime > this.flowProcessor.configuration.getThresholdWaitTimeToReady()) {
                                stateMode = ENTER_MONITOR_QUEUE;
                            } else {
                                stateMode = ENTER_EXEC_QUEUE;
                            }

                        } else {
                            // time over
                            stateMode = TERMINATE_BY_TIMEOVER;
                        }

                    } else {
                        stateMode = ENTER_EXEC_QUEUE2;
                    }

                    adjustThread(runningCounter.get());


                } catch (FlowProcessException fpe) {
                    stateMode = TERMINATE_BY_FLOW;
                    //logger.error("InterruptedException", ite);

                } catch (RuntimeException re) {
                    stateMode = TERMINATE_BY_ERROR;
                    logger.error("runtime error", re);

                } catch (InterruptedException ite) {
                    stateMode = TERMINATE_BY_TIMEOVER;
                    //logger.error("InterruptedException", ite);

                } catch (Throwable e) {
                    stateMode = TERMINATE_BY_ERROR;

                    logger.error("error", e);

                } finally {

                    if (plan != null) {

                        switch (stateMode) {
                            case TERMINATE_BY_ERROR:
                            case TERMINATE_BY_USER:
                            case TERMINATE_BY_TIMEOVER:
                            case TERMINATE_BY_FLOW:

                                long intervalTime4Flow = plan.getIntervalTime4Flow();
                                if(intervalTime4Flow>0 && workEvent!=null) {
                                    WorkEvent origin = workEvent.origin();
                                    origin.reset();
                                    WorkEvent newEvent = WorkEventFactory.createWithOrigin(null, origin);

                                    this.flowProcessor.addWorkSchedule(plan, newEvent, intervalTime4Flow);


                                }else {
                                    plan.inactive();
                                }

                                break;

                            case ENTER_EXEC_QUEUE:
                                this.flowProcessor.addWorkSchedule(plan);
                                break;

                            case ENTER_MONITOR_QUEUE:
                                this.flowProcessor.workChecker.addWorkStatusWrapper(plan);
                                break;

                            case ENTER_EXEC_QUEUE2:
                                try {
                                    this.flowProcessor.workQueue.put(plan.enterQueue(false));
                                } catch (InterruptedException ite) {

                                }

                        }
                    }


                    //if(plan!=null)
                    // logger.debug(maxWaitTime+" "+stateMode+">> "+this.flowProcessor.workQueue.size() + " / "+this.flowProcessor.workChecker.statusWrappers.size());

                }
            }

            this.flowProcessor.removeThreadWorker(this);

        }


        private void stopWorking() {
            this.isThreadRunning = false;
            this.interrupt();
        }

    }

    private static class WorkScheduleWrapper {

        private PlanImpl plan = null;

        private WorkEvent workEvent = null;

        WorkScheduleWrapper(PlanImpl plan) {
            this.plan = plan;
        }

        WorkScheduleWrapper(PlanImpl plan, WorkEvent workEvent) {
            this(plan);
            this.workEvent = workEvent;
        }


        PlanImpl getPlan() {
            return plan;
        }

        WorkEvent getWorkEvent() {
            return workEvent;
        }


    }


    /****************************************
     *
     * Thread class for checking work status.
     */
    static final class WorkChecker extends Thread {

        private FlowProcessor flowProcessor;

        private BlockingQueue<WorkScheduleWrapper> statusWrappers = new LinkedBlockingQueue<>();

        WorkChecker(FlowProcessor flowProcessor) {
            this.flowProcessor = flowProcessor;
        }


        void addWorkStatusWrapper(PlanImpl sb) {
            statusWrappers.add(new WorkScheduleWrapper(sb));
        }

        void addWorkStatusWrapper(PlanImpl sb, WorkEvent event) {
            statusWrappers.add(new WorkScheduleWrapper(sb, event));
        }

        @Override
        public void run() {

            long thresholdWaitTimeToReady = this.flowProcessor.configuration.getThresholdWaitTimeToReady();
            while (flowProcessor.isWorking) {

                try {
                    WorkScheduleWrapper workScheduleWrapper = statusWrappers.poll(5, TimeUnit.SECONDS);


                    if (workScheduleWrapper != null) {
                        PlanImpl plan = workScheduleWrapper.getPlan();

                        long nextExecuteTime = -1;

                        WorkEvent event = workScheduleWrapper.getWorkEvent();
                        if (event != null) {
                            nextExecuteTime = event.getFireTime();
                        }

                        if (nextExecuteTime < 0) {
                            nextExecuteTime = plan.getNextExecuteTime();
                        }


                        long gap = System.currentTimeMillis()  - nextExecuteTime;
                        if(plan.isStrictMode()) {
                            gap = gap + thresholdWaitTimeToReady;
                        }
                        if (gap >= 0) {

                            if (event != null) {
                                flowProcessor.addWorkSchedule(plan, event);
                            } else {
                                flowProcessor.addWorkSchedule(plan);
                            }


                        } else {


                            Thread.sleep(1);
                            statusWrappers.add(workScheduleWrapper);


                        }
                    }
                } catch (Exception e) {

                    try {
                        Thread.sleep(200);
                    } catch (Exception ee) {

                    }
                }
            }

            statusWrappers.clear();
        }
    }


    private <T> T invokeAnnonations(T instance) throws IllegalAccessException {
        Method[] methods = instance.getClass().getDeclaredMethods();
        for (Method method : methods) {
            WorkInfo annotation = method.getAnnotation(WorkInfo.class);
            if (annotation != null) {
                //field.setAccessible(true);
                //field.set(instance, annotation.isAsync());
            }
        }
        return instance;
    }

    /**
     * Returns a object
     *
     * @param clazz class
     * @param <T>   Type
     * @return instance
     * @throws IllegalAccessException if the class or its nullary constructor is not accessible.
     * @throws InstantiationException if this Class represents an abstract class, an interface, an array class, a primitive type, or void; or if the class has no nullary constructor; or if the instantiation fails for some other reason.
     */
    public <T> T get(Class clazz) throws IllegalAccessException, InstantiationException {
        T instance = (T) clazz.newInstance();
        instance = invokeAnnonations(instance);
        return instance;
    }


}
