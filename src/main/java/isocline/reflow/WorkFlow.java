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
import isocline.reflow.event.WorkEventKey;
import isocline.reflow.flow.*;
import isocline.reflow.flow.func.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 *
 */
public class WorkFlow<T> {


    public final static String START = "ReFlow::start";


    public final static String FINISH = "ReFlow::inactive";


    public final static String ERROR = "error::*";



    private String[] regReadyEventNameArray = new String[]{WorkFlow.START};

    private String[] prestepRegEventNameArray = null;

    private StringBuilder regRunAsyncId = new StringBuilder();

    private boolean isSetFinish = false;


    private FunctionExecutor lastFuncExecutor = null;

    private FunctionExecutor lastSyncFuncExecutor = null;

    private EventRepository<String, List<FunctionExecutor>> eventRepository = new EventRepository();

    private List<FunctionExecutor> functionExecutorList = new ArrayList<>();


    public static WorkFlow create() {
        WorkFlowWrapper workFlowWrapper = new WorkFlowWrapper(new WorkFlow());
        workFlowWrapper.run(WorkFlow::dummy);

        return workFlowWrapper;
    }

    protected WorkFlow() {

    }

    private static void dummy() {

    }

    private void clearLastFunctionExecutor() {
        this.lastFuncExecutor = null;
        this.lastSyncFuncExecutor = null;
    }

    private void bindEventRepository(String eventName, FunctionExecutor functionExecutor) {


        List<FunctionExecutor> functionExecutorList = this.eventRepository.computeIfAbsent(eventName, k -> new ArrayList<>());

        functionExecutorList.add(functionExecutor);

    }

    public WorkFlow<T> onError(String... eventNames) {
        for (int i = 0; i < eventNames.length; i++) {
            eventNames[i] = WorkEventKey.PREFIX_ERROR + eventNames[i];
        }

        wait(eventNames);

        return this;
    }

    public WorkFlow<T> wait(String... eventNames) {

        clearLastFunctionExecutor();


        String[] newEventNameArray;
        if (regReadyEventNameArray != null) {
            newEventNameArray = new String[regReadyEventNameArray.length + eventNames.length];

            System.arraycopy(regReadyEventNameArray, 0, newEventNameArray, 0, regReadyEventNameArray.length);
            System.arraycopy(eventNames, 0, newEventNameArray, regReadyEventNameArray.length, eventNames.length);

        } else {
            newEventNameArray = eventNames;
        }

        regReadyEventNameArray = newEventNameArray;

        return this;
    }

    public WorkFlow<T> waitAll() {

        clearLastFunctionExecutor();

        if (this.regRunAsyncId.length() > 0) {
            this.waitAll(this.regRunAsyncId.toString());
            this.regRunAsyncId = new StringBuilder();
        }

        return this;
    }


    public WorkFlow<T> waitAll(String... eventNames) {


        clearLastFunctionExecutor();

        StringBuilder fullEventName = new StringBuilder();
        for (int i = 0; i < eventNames.length; i++) {

            fullEventName.append(eventNames[i]);

            if (i < (eventNames.length - 1)) {
                fullEventName.append("&");
            }
        }


        String[] newEventNameArray = new String[1];
        newEventNameArray[0] = fullEventName.toString();

        newEventNameArray[newEventNameArray.length - 1] = fullEventName.toString();

        regReadyEventNameArray = newEventNameArray;

        return this;
    }


    public WorkFlow<T> wait(WorkFlow... workFlows) {

        String[] eventNameArray = new String[workFlows.length];

        for (int i = 0; i < eventNameArray.length; i++) {
            eventNameArray[i] = workFlows[i].cursor();
        }
        return wait(eventNameArray);
    }


    public WorkFlow<T> waitAll(WorkFlow... workFlows) {
        String[] eventNameArray = new String[workFlows.length];

        for (int i = 0; i < eventNameArray.length; i++) {
            eventNameArray[i] = workFlows[i].cursor();
        }
        return waitAll(eventNameArray);
    }


    public WorkFlow<T> onError(WorkFlow... workFlows) {
        String[] eventNameArray = new String[workFlows.length];

        for (int i = 0; i < eventNameArray.length; i++) {
            eventNameArray[i] = workFlows[i].cursor();
        }
        return onError(eventNameArray);
    }


    public WorkFlow<T> onError(Class... errorClassess) {
        String[] eventNameArray = new String[errorClassess.length];

        for (int i = 0; i < eventNameArray.length; i++) {
            eventNameArray[i] = errorClassess[i].getName();
        }
        return onError(eventNameArray);
    }


    public WorkFlow<T> onError(WorkEventConsumer consumer) {
        return onError(this).accept(consumer);
    }

    /**
     * @param funcExecutor
     * @param reset
     * @return
     */
    private boolean bindEvent(FunctionExecutor funcExecutor, boolean reset) {

        if (regReadyEventNameArray == null) {
            return false;
        }
        for (String eventName : regReadyEventNameArray) {

            String[] subEventNames = eventRepository.setBindEventNames(eventName);

            if (funcExecutor == null) {
                funcExecutor = new FunctionExecutor();
            }


            bindEventRepository(eventName, funcExecutor);
            //eventRepository.put(eventName, funcExecutor);

            for (String subEventName : subEventNames) {

                if (!eventName.equals(subEventName)) {
                    bindEventRepository(subEventName, funcExecutor);
                    //eventRepository.put(subEventName, funcExecutor);
                }

            }
        }

        if (reset) {

            prestepRegEventNameArray = regReadyEventNameArray;

            boolean isStartEvent = false;
            if (regReadyEventNameArray != null && regReadyEventNameArray.length == 1 && regReadyEventNameArray[0].equals(START)) {
                isStartEvent = true;
            }

            regReadyEventNameArray = null;

            if (isStartEvent) {
                return false;
            }

        }


        return true;
    }


    public WorkFlow<T> runAsync(Runnable... execObject) {

        WorkFlow f = this;

        for (Runnable c : execObject) {

            f = processRunAsync(c, null);
        }


        return f;

        //return this.waitAll();

    }


    /*
    public WorkFlow<T> runAsync(Consumer<? super T> execObject) {
        return processRunAsync(execObject, null);
    }


    public WorkFlow<T> runAsync(Consumer<? super T> execObject, String eventName) {
        return processRunAsync(execObject, eventName);
    }


    public WorkFlow<T> runAsync(Consumer<T> execObject, int count) {

        for (int i = 0; i < count; i++) {
            processRunAsync(execObject, null);
        }

        return this.waitAll();
    }
    */


    public WorkFlow<T> runAsync(Runnable execObject, String eventName) {
        return processRunAsync(execObject, eventName);
    }

    public WorkFlow<T> runAsync(Runnable execObject, int count) {

        for (int i = 0; i < count; i++) {
            processRunAsync(execObject, null);
        }

        return this.waitAll();
    }


    public WorkFlow<T> runAsync(WorkEventConsumer... execObject) {


        WorkFlow f = this;

        for (WorkEventConsumer c : execObject) {

            f = processRunAsync(c, null);
        }

        return f;

        //return this.waitAll();
    }


    public WorkFlow runAsync(WorkEventConsumer execObject, String fireEventName) {
        return processRunAsync(execObject, fireEventName);
    }


    public WorkFlow runAsync(WorkEventConsumer execObject, int count) {
        WorkFlow workFlow = null;
        for (int i = 0; i < count; i++) {
            workFlow = processRunAsync(execObject, null);
        }

        return workFlow;
    }


    public <R> WorkFlow<R> extractAsync(WorkEventFunction<? extends R>... execObject) {
        WorkFlow result = null;

        for (WorkEventFunction c : execObject) {

            result = processRunAsync(c, null);
        }

        return result;
    }


    public <R> WorkFlow<R> extractAsync(WorkEventFunction<? extends R> execObject, String fireEventName) {
        return processRunAsync(execObject, fireEventName);
    }


    public <R> WorkFlow<R> extractAsync(WorkEventFunction<? extends R> execObject, int count) {
        WorkFlow workFlow = null;
        for (int i = 0; i < count; i++) {
            workFlow = processRunAsync(execObject, null);
        }

        return workFlow;
    }


    public <R> WorkFlow<R> extract(WorkEventFunction<? extends R>... execObjects) {

        for (WorkEventFunction execObject : execObjects) {
            processRunAsync(execObject, null);
        }

        return (WorkFlow<R>) waitAll();
    }

    protected boolean beforeProcessRunAsync(FunctionExecutor executor) {
        return true;
    }


    /**
     * @param execObject
     * @return
     */
    private WorkFlow processRunAsync(Object execObject, String eventName) {



        final FunctionExecutor asyncFunc = new FunctionExecutor(execObject);
        this.lastFuncExecutor = asyncFunc;
        if (eventName != null) {
            this.lastFuncExecutor.setFireEventName(eventName);
        }

        if(this.customWorkFlow!=null && !this.customWorkFlow.beforeProcessRunAsync(asyncFunc)) {
            return this;
        }


        if (this.regRunAsyncId.length() > 0) {
            this.regRunAsyncId.append("&");
        }
        this.regRunAsyncId.append(asyncFunc.getFireEventUUID());

        if (this.lastSyncFuncExecutor != null) {
            String eventNm = this.lastSyncFuncExecutor.getFireEventUUID();
            bindEventRepository(eventNm, asyncFunc);
        }

        //bindEventRepository(eventName, this.lastFuncExecutor);
        //eventRepository.put(eventName, this.lastFuncExecutor);

        boolean isRegist = bindEvent(asyncFunc, false);


        if (!isRegist && this.lastSyncFuncExecutor == null) {
            functionExecutorList.add(asyncFunc);
        }

        return this;
    }

    public WorkFlow fireEvent(String eventName, long delayTime) {

        if (eventName == null || eventName.trim().length() == 0) {
            throw new IllegalArgumentException("Event name is empty.");
        }

        return processNext(null, new FuntionalInteraceContext().with($->{
            $.allowNullFunction = true;
            $.fireEventName = eventName;
            $.delayTime = delayTime;
        }));

    }


    public WorkFlow fireEventOnError(String eventName, long time) {
        if (this.lastFuncExecutor != null) {
            String uuid = this.lastFuncExecutor.getFireEventUUID();
            this.onError(uuid);


            return this.fireEvent(eventName, time);
        } else {
            throw new IllegalStateException("Reflow position is not valid");
        }
    }


    public WorkFlow count(int maxCount) {
        return this;
    }


    public WorkFlow retryOnError(int maxCount, long delayTime) {


        if (this.lastFuncExecutor != null && prestepRegEventNameArray != null && prestepRegEventNameArray.length > 0) {


            String uuid = this.lastFuncExecutor.getFireEventUUID();

            this.onError(uuid);

            return processNext(null, new FuntionalInteraceContext().with($->{
                $.fireEventName = prestepRegEventNameArray[0];
                $.allowNullFunction = true;
                $.delayTime = delayTime;
                $.maxCallCount = maxCount;

            }));

        } else {
            throw new IllegalStateException("retryOnError position is not valid");
        }


    }


    public WorkFlow branch(ReturnEventFunction execObject) {
        return processNext(execObject);
    }


    public WorkFlow when(CheckFunction execObject) {
        return processNext(execObject);
    }


    public WorkFlow limit(int maxCount) {
        return when(event -> event.count() <= maxCount);
    }

    public WorkFlow run(ThrowableRunFunction execObject) {
        return processNext(execObject);
    }


    public WorkFlow run(ThrowableRunFunction execObject, String eventName) {
        return processNext(execObject, new FuntionalInteraceContext().with($->{
            $.fireEventName = eventName;
        }));
    }


    /*
    public WorkFlow<T> apply(Consumer<? super T> execObject) {
        return processNext(execObject, null, false);
    }


    public WorkFlow<T> apply(Consumer<? super T> execObject, String fireEventName) {
        return processNext(execObject, fireEventName, true);
    }

    public WorkFlow<T> apply(Consumer<? super T> execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        return processNext(execObject, null, false, false, 0, -1, fnExecFeatureFunction);
    }
    */


    public <R> WorkFlow<R> apply(WorkEventFunction<? extends R> execObject) {
        return (WorkFlow<R>) processNext(execObject);
    }


    public <R> WorkFlow<R> run(WorkEventFunction<? extends R> execObject, String fireEventName) {
        return (WorkFlow<R>) processNext(execObject, new FuntionalInteraceContext().with($->{
            $.fireEventName = fireEventName;
        }));

    }


    public WorkFlow<T> accept(WorkEventConsumer execObject) {
        return processNext(execObject);
    }

    public WorkFlow<T> accept(WorkEventConsumer execObject, String eventName) {
        return processNext(execObject, new FuntionalInteraceContext().with($->{
            $.fireEventName = eventName;
        }));

    }


    public WorkFlow<T> run(ThrowableRunFunction execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        return processNext(execObject, new FuntionalInteraceContext().with($->{
            $.featureFunction = fnExecFeatureFunction;
        }));

    }


    public WorkFlow<T> accept(WorkEventConsumer execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        return processNext(execObject, new FuntionalInteraceContext().with($->{
            $.featureFunction = fnExecFeatureFunction;
        }));
    }

    public <R> WorkFlow<R> apply(WorkEventFunction<? extends R> execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        return (WorkFlow<R>) processNext(execObject, new FuntionalInteraceContext().with($->{
            $.featureFunction = fnExecFeatureFunction;
        }));

    }


    public <R> WorkFlow<R> trans(Function<? super T, ? extends R> mapper) {
        return (WorkFlow<R>) processNext(mapper);
    }


    public WorkFlow delay(long delayTime) {
        String newEventName = "tmp::" + Math.random();
        this.fireEvent(newEventName, delayTime);
        return wait(newEventName);
    }


    public WorkFlow flag(String eventName) {
        return fireEvent(eventName, 0).wait(eventName);
    }



    static void processTimeout(WorkEvent event) {
        Thread t = event.getTimeoutThread();
        t.interrupt();
    }



    WorkFlow<T> processNext(Object functionalInterface) {
        return processNext(functionalInterface, new FuntionalInteraceContext());
    }


    protected boolean beforeProcessNext(FunctionExecutor functionExecutor) {
        return true;
    }

        /**
         *
         * @param functionalInterface
         * @param ctx
         * @return
         */
    WorkFlow<T> processNext(Object functionalInterface, FuntionalInteraceContext ctx) {




        if (!ctx.allowNullFunction && functionalInterface == null) {
            throw new IllegalArgumentException("function interface is null");
        }

        FunctionExecutor newFuncExecutor = new FunctionExecutor(functionalInterface);

        if(this.customWorkFlow!=null && !this.customWorkFlow.beforeProcessNext(newFuncExecutor)) {
            return this;
        }

        if (ctx.featureFunction != null) {
            ctx.featureFunction.apply(newFuncExecutor);
        }

        long timeout = newFuncExecutor.getTimeout();

        if (timeout > 0) {

            FunctionExecutor timeoutProcess = new FunctionExecutor((WorkEventConsumer) WorkFlow::processTimeout);

            String[] timeoutEventNames = newFuncExecutor.getTimeoutFireEventNames();

            String firstTimeoutEventName;
            if (timeoutEventNames != null && timeoutEventNames.length > 0) {
                firstTimeoutEventName = timeoutEventNames[0];
            } else {
                firstTimeoutEventName = "timeout-" + hashCode();
                newFuncExecutor.timeout(timeout, firstTimeoutEventName);

                timeoutProcess.setLastExecutor(true);

            }


            bindEventRepository(firstTimeoutEventName, timeoutProcess);
        }

        if (ctx.isLastExecute) {
            newFuncExecutor.setLastExecutor(true);
        }
        if (ctx.fireEventName != null) {
            newFuncExecutor.setFireEventName(ctx.fireEventName);
            newFuncExecutor.setDelayTimeFireEvent(ctx.delayTime);

            if (ctx.maxCallCount > 0)
                newFuncExecutor.setMaxCallCount(ctx.maxCallCount);
        }


        boolean isInitialExecutor = false;
        if (this.lastFuncExecutor != null) {
            newFuncExecutor.setRecvEventName(this.lastFuncExecutor.getFireEventUUID());

            bindEventRepository(this.lastFuncExecutor.getFireEventUUID(), newFuncExecutor);
            //eventRepository.put(this.lastFuncExecutor.getFireEventUUID(), newFuncExecutor);
        } else {
            isInitialExecutor = true;
        }

        this.lastFuncExecutor = newFuncExecutor;
        this.lastSyncFuncExecutor = newFuncExecutor;


        boolean isRegist = bindEvent(this.lastFuncExecutor, true);

        if (!isRegist && isInitialExecutor) {
            functionExecutorList.add(this.lastFuncExecutor);
        }

        if (ctx.isLastExecute) {
            clearLastFunctionExecutor();
        }

        return this;
    }



    public WorkFlow end() {
        this.isSetFinish = true;
        return processNext(null, new FuntionalInteraceContext().with($->{
            $.allowNullFunction = true;
            $.isLastExecute = true;
        }));

    }

    public boolean isSetFinish() {
        return this.isSetFinish;
    }


    public FunctionExecutor getNextExecutor(WorkEvent event) {

        String eventName = event.getEventName();
        if (eventName != null && (WorkFlow.FINISH.equals(eventName) || eventName.indexOf(WorkEventKey.PREFIX_FUNC_UUID) == 0)) {
            return null;
        }


        AtomicInteger counter = event.origin().getCounter(WorkEventKey.COUNTER_FUNC_EXEC);

        //IndexOutOfBoundsException

        FunctionExecutor exec;
        try {
            exec = functionExecutorList.get(counter.get());
        } catch (IndexOutOfBoundsException ie) {
            return null;
        }
        if (exec != null) {
            counter.addAndGet(1);
        }

        return exec;
    }

    public boolean existNextFunctionExecutor(WorkEvent event) {
        AtomicInteger counter = event.origin().getCounter(WorkEventKey.COUNTER_FUNC_EXEC);

        return counter.get() < functionExecutorList.size();

    }


    public FunctionExecutorList getFunctionExecutorList(WorkEvent event, String eventName) {
        if (eventName == null) return null;


        SimultaneousEventSet simultaneousEventSet = eventRepository.getSimultaneousEventSet(eventName);

        if (simultaneousEventSet == null || simultaneousEventSet.isRaiseEventReady(event, eventName)) {
            FunctionExecutorList functionExecutorList = (FunctionExecutorList) event.origin().get(WorkEventKey.PREFIX_FUNCTION + eventName);
            if (functionExecutorList == null) {
                List<FunctionExecutor> list = this.eventRepository.get(eventName);
                if (list == null) {
                    return null;
                }

                functionExecutorList = new FunctionExecutorList(list, event, eventName);
                event.origin().put(WorkEventKey.PREFIX_FUNCTION + eventName, functionExecutorList);

            }


            return functionExecutorList;

        }

        return null;


    }


    public String cursor() {
        if (this.lastFuncExecutor != null) {
            return this.lastFuncExecutor.getFireEventUUID();
        }

        return null;
    }

    static class FuntionalInteraceContext {

        private String fireEventName;

        private boolean allowNullFunction = true;
        private boolean isLastExecute = false;
        private long delayTime=0;
        private int maxCallCount = -1;
        private FnExecFeatureFunction featureFunction;

        static FuntionalInteraceContext create() {
            return new FuntionalInteraceContext();

        }

        FuntionalInteraceContext delayTime(long delayTime) {
            this.delayTime = delayTime;
            return this;
        }

        FuntionalInteraceContext maxCallCount(int maxCallCount) {
            this.maxCallCount = maxCallCount;
            return this;
        }

        FuntionalInteraceContext featureFunction(FnExecFeatureFunction featureFunction) {
            this.featureFunction = featureFunction;
            return this;
        }

        FuntionalInteraceContext allowFunctionNull(boolean allowFuncInfNull) {
            this.allowNullFunction = allowFuncInfNull;
            return this;
        }

        FuntionalInteraceContext lastExecute(boolean isLastExecute) {
            this.isLastExecute = isLastExecute;
            return this;
        }

        public FuntionalInteraceContext with(
                Consumer<FuntionalInteraceContext> builderFunction) {
            builderFunction.accept(this);
            return this;
        }


    }

    private CustomWorkFlow customWorkFlow = null;

    public CustomWorkFlow applyPattern(CustomWorkFlowBuilder builder) {

        customWorkFlow = builder.build(this);

        return customWorkFlow;

    }


    public static void main(String[] args) {
        FuntionalInteraceContext ctx = new FuntionalInteraceContext().with($->{

            $.delayTime = 123;
            $.maxCallCount=12;
        });


        System.err.println( ctx.delayTime );
    }
}
