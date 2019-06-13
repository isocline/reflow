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
package isocline.reflow.flow;

import isocline.reflow.WorkEvent;
import isocline.reflow.WorkFlow;
import isocline.reflow.WorkFlowPattern;
import isocline.reflow.event.EventRepository;
import isocline.reflow.event.SimultaneousEventSet;
import isocline.reflow.event.WorkEventKey;
import isocline.reflow.flow.func.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 *
 *
 */
public class WorkFlowImpl<T> implements WorkFlow<T> {





    private int funcExecSequence = 0;

    private String[] regReadyEventNameArray = new String[]{WorkFlow.START};

    private String[] prestepRegEventNameArray = null;

    private StringBuilder regRunAsyncId = new StringBuilder();

    private boolean isSetFinish = false;


    private FunctionExecutor lastFuncExecutor = null;

    private FunctionExecutor lastSyncFuncExecutor = null;

    private EventRepository<String, List<FunctionExecutor>> eventRepository = new EventRepository();

    private List<FunctionExecutor> functionExecutorList = new ArrayList<FunctionExecutor>();


    WorkFlowImpl() {

    }

    private void clearLastFunctionExecutor() {
        this.lastFuncExecutor = null;
        this.lastSyncFuncExecutor = null;
    }

    private void bindEventRepository(String eventName, FunctionExecutor functionExecutor) {


        List<FunctionExecutor> functionExecutorList = this.eventRepository.get(eventName);
        if (functionExecutorList == null) {
            functionExecutorList = new ArrayList<>();
            this.eventRepository.put(eventName, functionExecutorList);
        }

        functionExecutorList.add(functionExecutor);

    }

    public WorkFlow onError(String... eventNames) {
        String[] inputEventNameArray = eventNames;
        for (int i = 0; i < inputEventNameArray.length; i++) {
            inputEventNameArray[i] = "error::" + inputEventNameArray[i];
        }

        wait(inputEventNameArray);

        return this;
    }

    public WorkFlow wait(String... eventNames) {

        clearLastFunctionExecutor();

        String[] inputEventNameArray = eventNames;


        String[] newEventNameArray = null;
        if (regReadyEventNameArray != null) {
            newEventNameArray = new String[regReadyEventNameArray.length + inputEventNameArray.length];

            System.arraycopy(regReadyEventNameArray, 0, newEventNameArray, 0, regReadyEventNameArray.length);
            System.arraycopy(inputEventNameArray, 0, newEventNameArray, regReadyEventNameArray.length, inputEventNameArray.length);

        } else {
            newEventNameArray = inputEventNameArray;
        }

        regReadyEventNameArray = newEventNameArray;

        return this;
    }

    public WorkFlow waitAll() {

        clearLastFunctionExecutor();

        if (this.regRunAsyncId.length() > 0) {
            this.waitAll(this.regRunAsyncId.toString());
            this.regRunAsyncId = new StringBuilder();
        }

        return this;
    }


    public WorkFlow waitAll(String... eventNames) {


        clearLastFunctionExecutor();

        String fullEventName = "";
        for (int i = 0; i < eventNames.length; i++) {

            fullEventName = fullEventName + eventNames[i];

            if (i < (eventNames.length - 1)) {
                fullEventName = fullEventName + "&";
            }
        }


        String[] newEventNameArray = new String[1];
        newEventNameArray[0] = fullEventName;

        newEventNameArray[newEventNameArray.length - 1] = fullEventName;

        regReadyEventNameArray = newEventNameArray;

        return this;
    }


    @Override
    public WorkFlow wait(WorkFlow... workFlows) {

        String[] eventNameArray = new String[workFlows.length];

        for (int i = 0; i < eventNameArray.length; i++) {
            eventNameArray[i] = workFlows[i].cursor();
        }
        return wait(eventNameArray);
    }

    @Override
    public WorkFlow waitAll(WorkFlow... workFlows) {
        String[] eventNameArray = new String[workFlows.length];

        for (int i = 0; i < eventNameArray.length; i++) {
            eventNameArray[i] = workFlows[i].cursor();
        }
        return waitAll(eventNameArray);
    }

    @Override
    public WorkFlow onError(WorkFlow... workFlows) {
        String[] eventNameArray = new String[workFlows.length];

        for (int i = 0; i < eventNameArray.length; i++) {
            eventNameArray[i] = workFlows[i].cursor();
        }
        return onError(eventNameArray);
    }


    @Override
    public WorkFlow onError(Class... errorClassess) {
        String[] eventNameArray = new String[errorClassess.length];

        for (int i = 0; i < eventNameArray.length; i++) {
            eventNameArray[i] = errorClassess[i].getName();
        }
        return onError(eventNameArray);
    }


    @Override
    public WorkFlow onError() {
        return onError(this);
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


    public WorkFlow runAsync(Runnable... execObject) {

        for(Runnable c:execObject) {

            processRunAsync(c, null);
        }

        return this.waitAll();

    }

    public WorkFlow runAsync(Consumer execObject) {
        return processRunAsync(execObject, null);
    }

    public WorkFlow runAsync(Runnable execObject, String eventName) {
        return processRunAsync(execObject, eventName);
    }

    public WorkFlow runAsync(Consumer execObject, String eventName) {
        return processRunAsync(execObject, eventName);
    }


    public WorkFlow runAsync(Runnable execObject, int count) {

        for (int i = 0; i < count; i++) {
            processRunAsync(execObject, null);
        }

        return this.waitAll();
    }


    public WorkFlow runAsync(Consumer execObject, int count) {

        for (int i = 0; i < count; i++) {
            processRunAsync(execObject, null);
        }

        return this.waitAll();
    }


    @Override
    public WorkFlow runAsync(WorkEventConsumer... execObject) {



        for(WorkEventConsumer c:execObject) {

            processRunAsync(c, null);
        }

        return this.waitAll();
    }

    @Override
    public WorkFlow runAsync(WorkEventConsumer execObject, String fireEventName) {
        return processRunAsync(execObject, fireEventName);
    }

    @Override
    public WorkFlow runAsync(WorkEventConsumer execObject, int count) {
        WorkFlow workFlow = null;
        for (int i = 0; i < count; i++) {
            workFlow = processRunAsync(execObject, null);
        }

        return workFlow;
    }


    @Override
    public WorkFlow applyAsync(WorkEventFunction... execObject) {
        WorkFlow result = null;

        for(WorkEventFunction c:execObject) {

            result = processRunAsync(c, null);
        }

        return result;
    }

    @Override
    public WorkFlow applyAsync(WorkEventFunction execObject, String fireEventName) {
        return processRunAsync(execObject, fireEventName);
    }

    @Override
    public WorkFlow applyAsync(WorkEventFunction execObject, int count) {
        WorkFlow workFlow = null;
        for (int i = 0; i < count; i++) {
            workFlow = processRunAsync(execObject, null);
        }

        return workFlow;
    }

    public WorkFlow mapAsync(WorkEventFunction... execObjects) {

        for (WorkEventFunction execObject : execObjects) {
            processRunAsync(execObject, null);
        }

        return waitAll();
    }


    /**
     * @param execObject
     * @return
     */
    private WorkFlowImpl processRunAsync(Object execObject, String eventName) {

        final FunctionExecutor asyncFunc = new FunctionExecutor(execObject);
        ;
        this.lastFuncExecutor = asyncFunc;
        if (eventName != null) {
            this.lastFuncExecutor.setFireEventName(eventName);
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

    public WorkFlowImpl fireEvent(String eventName, long delayTime) {

        if (eventName == null || eventName.trim().length() == 0) {
            throw new IllegalArgumentException("Event name is empty.");
        }

        return processNext(null, eventName, true, false, delayTime);
    }

    @Override
    public WorkFlow fireEventOnError(String eventName, long time) {
        if (this.lastFuncExecutor != null) {
            String uuid = this.lastFuncExecutor.getFireEventUUID();
            this.onError(uuid);


            return this.fireEvent(eventName, time);
        } else {
            throw new IllegalStateException("Reflow position is not valid");
        }
    }

    @Override
    public WorkFlow count(int maxCount) {
        return this;
    }


    @Override
    public WorkFlow retryOnError(int maxCount, long delayTime) {


        if (this.lastFuncExecutor != null && prestepRegEventNameArray != null && prestepRegEventNameArray.length > 0) {


            String uuid = this.lastFuncExecutor.getFireEventUUID();

            this.onError(uuid);


            return processNext(null, prestepRegEventNameArray[0], true, false, delayTime, maxCount, null);
        } else {
            throw new IllegalStateException("retryOnError position is not valid");
        }


    }

    @Override
    public WorkFlow branch(ReturnEventFunction execObject) {
        return processNext(execObject, null, false);
    }

    @Override
    public WorkFlow check(CheckFunction execObject) {
        return processNext(execObject, null, false);
    }

    @Override
    public WorkFlow check(int maxCount) {
        return check(event -> {
            if (event.count() <= maxCount) return true;
            else return false;
        });
    }

    public WorkFlowImpl next(ThrowableRunFunction execObject) {
        return processNext(execObject, null, false);
    }

    public WorkFlowImpl next(Consumer<? super T> execObject) {
        return processNext(execObject, null, false);
    }

    public WorkFlowImpl next(ThrowableRunFunction execObject, String eventName) {
        return processNext(execObject, eventName, false);
    }

    public WorkFlowImpl next(Consumer<? super T> execObject, String eventName) {
        return processNext(execObject, eventName, true);
    }


    public WorkFlowImpl next(WorkEventConsumer execObject) {
        return processNext(execObject, null, false);
    }

    public WorkFlowImpl next(WorkEventConsumer execObject, String eventName) {
        return processNext(execObject, eventName, false);
    }

    public WorkFlowImpl next(WorkEventFunction execObject) {
        return processNext(execObject, null, false);
    }

    public WorkFlowImpl next(WorkEventFunction execObject, String eventName) {
        return processNext(execObject, eventName, false);
    }

    @Override
    public WorkFlow next(ThrowableRunFunction execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        return processNext(execObject, null, false , false, 0 , -1, fnExecFeatureFunction);
    }


    @Override
    public WorkFlow next(Consumer<? super T> execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        return processNext(execObject, null, false , false, 0 , -1, fnExecFeatureFunction);
    }

    @Override
    public WorkFlow next(WorkEventConsumer execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        return processNext(execObject, null, false , false, 0 , -1, fnExecFeatureFunction);
    }

    @Override
    public WorkFlow next(WorkEventFunction execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        return processNext(execObject, null, false , false, 0 , -1, fnExecFeatureFunction);
    }







    @Override
    public WorkFlow pattern(WorkFlowPattern pattern, WorkFlowPatternFunction... funcs) {

        pattern.startFlow(this);

        for(int i=0;i<funcs.length;i++) {
            funcs[i].design();
            pattern.middleFlow(this, i);
        }

        pattern.endFlow(this);
        return this;
    }

    WorkFlowImpl processNext(Object execObject, String eventName, boolean allowFuncInfNull) {
        return processNext(execObject, eventName, allowFuncInfNull, false, 0);
    }


    WorkFlowImpl processNext(Object functionalInterface, String fireEventName, boolean allowFuncInfNull, boolean isLastExecuteMethod, long delayTime) {
        return processNext(functionalInterface, fireEventName, allowFuncInfNull, isLastExecuteMethod, delayTime, -1,null);
    }


    static void processTimeout(WorkEvent event) {
        Thread t = event.getTimeoutThread();
        t.interrupt();
    }

    WorkFlowImpl processNext(Object functionalInterface, String fireEventName, boolean allowFuncInfNull, boolean isLastExecuteMethod, long delayTime, int maxCallCount, FnExecFeatureFunction featureFunction) {

        if (!allowFuncInfNull && functionalInterface == null) {
            throw new IllegalArgumentException("function interface is null");
        }

        FunctionExecutor newFuncExecutor = new FunctionExecutor(functionalInterface);

        if(featureFunction!=null) {
            featureFunction.apply(newFuncExecutor);
        }

        long timeout = newFuncExecutor.getTimeout();

        if(timeout>0) {

            FunctionExecutor timeoutProcess = new FunctionExecutor( (WorkEventConsumer) WorkFlowImpl::processTimeout);

            String[] timeoutEventNames = newFuncExecutor.getTimeoutFireEventNames();

            String firstTimeoutEventName = null;
            if(timeoutEventNames!=null && timeoutEventNames.length>0) {
                firstTimeoutEventName = timeoutEventNames[0];
            }else {
                firstTimeoutEventName = "timeout-"+hashCode();
                newFuncExecutor.timeout(timeout, firstTimeoutEventName);

                timeoutProcess.setLastExecutor(true);

            }


            bindEventRepository(firstTimeoutEventName, timeoutProcess);
        }

        if (isLastExecuteMethod) {
            newFuncExecutor.setLastExecutor(true);
        }
        if (fireEventName != null) {
            newFuncExecutor.setFireEventName(fireEventName);
            newFuncExecutor.setDelayTimeFireEvent(delayTime);

            if (maxCallCount > 0)
                newFuncExecutor.setMaxCallCount(maxCallCount);
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

        if (isLastExecuteMethod) {
            clearLastFunctionExecutor();
        }

        return this;
    }

    public WorkFlowImpl finish() {
        this.isSetFinish = true;
        return processNext(null, null, true, true, 0);
    }

    public boolean isSetFinish() {
        return this.isSetFinish;
    }


    public FunctionExecutor getNextExecutor(WorkEvent event) {

        String eventName = event.getEventName();
        if(eventName!=null && (WorkFlow.FINISH.equals(eventName) || eventName.indexOf(WorkEventKey.PREFIX_FUNC_UUID)==0)) {
            return null;
        }



        AtomicInteger counter = event.origin().getCounter(WorkEventKey.COUNTER_FUNC_EXEC);

        //IndexOutOfBoundsException

        FunctionExecutor exec = null;
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

        if (counter.get() < functionExecutorList.size()) {
            return true;
        }

        return false;
    }


    public FunctionExecutorList getFunctionExecutorList(WorkEvent event, String eventName) {
        if (eventName == null) return null;


        SimultaneousEventSet simultaneousEventSet = eventRepository.getSimultaneousEventSet(eventName);

        if (simultaneousEventSet == null || simultaneousEventSet.isRaiseEventReady(event, eventName)) {
            FunctionExecutorList functionExecutorList = (FunctionExecutorList) event.origin().get(WorkEventKey.PREFIX_FUNCTION+eventName);
            if(functionExecutorList==null) {
                List<FunctionExecutor> list =  this.eventRepository.get(eventName);
                if(list==null) {
                    return null;
                }

                functionExecutorList = new FunctionExecutorList(list, event, eventName);
                event.origin().put(WorkEventKey.PREFIX_FUNCTION+eventName, functionExecutorList);

            }


            if (functionExecutorList != null) {
                return functionExecutorList;
            }

        }

        return null;


    }

    @Override
    public String cursor() {
        if (this.lastFuncExecutor != null) {
            return this.lastFuncExecutor.getFireEventUUID();
        }

        return null;
    }


}
