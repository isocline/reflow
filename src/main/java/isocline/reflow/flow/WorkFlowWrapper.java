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
import isocline.reflow.flow.func.*;

import java.util.function.Consumer;
import java.util.function.Function;

public class WorkFlowWrapper<T> implements WorkFlow<T> {

    protected WorkFlow workFlowInstance;

    private String cursor;


    protected WorkFlowWrapper(WorkFlow workFlow) {
        this.workFlowInstance = workFlow;
        this.cursor = workFlow.cursor();
    }


    @Override
    public WorkFlow<T> wait(String... eventNames) {
        this.workFlowInstance.wait(eventNames);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> waitAll() {
        this.workFlowInstance.waitAll();
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> waitAll(String... eventNames) {
        this.workFlowInstance.waitAll(eventNames);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> onError(String... eventNames) {
        this.workFlowInstance.onError(eventNames);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> wait(WorkFlow... workFlows) {
        this.workFlowInstance.wait(workFlows);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> waitAll(WorkFlow... workFlows) {
        this.workFlowInstance.waitAll(workFlows);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> onError(WorkFlow... workFlows) {
        this.workFlowInstance.onError(workFlows);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> onError(Class... errorClasses) {
        this.workFlowInstance.onError(errorClasses);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> onError() {
        this.workFlowInstance.onError(this);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> runAsync(Runnable... execObject) {
        this.workFlowInstance.runAsync(execObject);
        return new WorkFlowWrapper(this.workFlowInstance);
    }



    @Override
    public WorkFlow<T> runAsync(Runnable execObject, String eventName) {
        this.workFlowInstance.runAsync(execObject, eventName);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> runAsync(Runnable execObject, int count) {
        this.workFlowInstance.runAsync(execObject, count);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    //@Override
    /*
    public WorkFlow<T> runAsync(Consumer execObject, int count) {
        this.workFlowInstance.runAsync(execObject, count);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    //@Override
    public WorkFlow<T> runAsync(Consumer execObject, String eventName) {
        this.workFlowInstance.runAsync(execObject, eventName);
        return new WorkFlowWrapper(this.workFlowInstance);
    }
    */

    @Override
    public WorkFlow<T> runAsync(WorkEventConsumer... execObject) {
        this.workFlowInstance.runAsync(execObject);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> runAsync(WorkEventConsumer execObject, String fireEventName) {
        this.workFlowInstance.runAsync(execObject, fireEventName);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> runAsync(WorkEventConsumer execObject, int count) {
        this.workFlowInstance.runAsync(execObject, count);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> supplyAsync(WorkEventFunction... execObject) {
        this.workFlowInstance.supplyAsync(execObject);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> supplyAsync(WorkEventFunction execObject, String fireEventName) {
        this.workFlowInstance.supplyAsync(execObject, fireEventName);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> supplyAsync(WorkEventFunction execObject, int count) {
        this.workFlowInstance.supplyAsync(execObject, count);
        return new WorkFlowWrapper(this.workFlowInstance);
    }



    @Override
    public <R> WorkFlow<R> supply(WorkEventFunction<? extends R>... execObjects) {
        this.workFlowInstance.supply(execObjects);
        return (WorkFlow<R>) new WorkFlowWrapper(this.workFlowInstance);
    }








    @Override
    public WorkFlow<T> branch(ReturnEventFunction execObject) {
        this.workFlowInstance.branch(execObject);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> when(CheckFunction execObject) {
        this.workFlowInstance.when(execObject);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> limit(int maxCount) {
        this.workFlowInstance.limit(maxCount);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> next(ThrowableRunFunction execObject) {
        this.workFlowInstance.next(execObject);
        return new WorkFlowWrapper(this.workFlowInstance);
    }


    @Override
    public WorkFlow<T> next(ThrowableRunFunction execObject, String eventName) {
        this.workFlowInstance.next(execObject, eventName);
        return new WorkFlowWrapper(this.workFlowInstance);
    }



    //@Override
    public WorkFlow<T> next(Consumer<? super T> execObject) {
        this.workFlowInstance.next(execObject);
        return new WorkFlowWrapper(this.workFlowInstance);
    }



    // @Override
    public WorkFlow<T> next(Consumer<? super T> execObject, String eventName) {
        this.workFlowInstance.next(execObject, eventName);
        return new WorkFlowWrapper(this.workFlowInstance);
    }


    @Override
    public WorkFlow<T> next(WorkEventConsumer execObject) {
        this.workFlowInstance.next(execObject);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> next(WorkEventConsumer execObject, String eventName) {
        this.workFlowInstance.next(execObject, eventName);
        return new WorkFlowWrapper(this.workFlowInstance);
    }



    @Override
    public <R> WorkFlow<R> next(WorkEventFunction<? extends R> execObject) {
        this.workFlowInstance.next(execObject);
        return (WorkFlow<R>) new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public <R> WorkFlow<R> next(WorkEventFunction<? extends R> execObject, String eventName) {
        this.workFlowInstance.next(execObject, eventName);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> next(ThrowableRunFunction  execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        this.workFlowInstance.next(execObject, fnExecFeatureFunction);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> next(Consumer<? super T> execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        this.workFlowInstance.next(execObject, fnExecFeatureFunction);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> next(WorkEventConsumer execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        this.workFlowInstance.next(execObject, fnExecFeatureFunction);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public <R> WorkFlow<R> next(WorkEventFunction<? extends R> execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        this.workFlowInstance.next(execObject, fnExecFeatureFunction);
        return new WorkFlowWrapper(this.workFlowInstance);
    }


    @Override
    public <R> WorkFlow<R> pipe(Function<? super T, ? extends R> mapper) {
        this.workFlowInstance.pipe(mapper);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> delay(long delayTime) {
        this.workFlowInstance.delay(delayTime);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> flag(String eventName) {
        this.workFlowInstance.flag(eventName);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> pattern(WorkFlowPattern pattern, WorkFlowPatternFunction... functions) {
        this.workFlowInstance.pattern(pattern, functions);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> fireEvent(String eventName, long time) {
        this.workFlowInstance.fireEvent(eventName, time);
        return new WorkFlowWrapper(this.workFlowInstance);

    }


    @Override
    public WorkFlow<T> fireEventOnError(String eventName, long time) {
        this.workFlowInstance.fireEventOnError(eventName, time);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> count(int maxCount) {
        this.workFlowInstance.count(maxCount);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> retryOnError(int maxCount, long delayTime) {
        this.workFlowInstance.retryOnError(maxCount, delayTime);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> end() {
        this.workFlowInstance.end();
        return new WorkFlowWrapper(this.workFlowInstance);

    }

    @Override
    public boolean isSetFinish() {
        return this.workFlowInstance.isSetFinish();
    }

    @Override
    public FunctionExecutor getNextExecutor(WorkEvent event) {
        return this.workFlowInstance.getNextExecutor(event);
    }

    @Override
    public boolean existNextFunctionExecutor(WorkEvent event) {
        return this.workFlowInstance.existNextFunctionExecutor(event);
    }

    @Override
    public FunctionExecutorList getFunctionExecutorList(WorkEvent event, String eventName) {
        return this.workFlowInstance.getFunctionExecutorList(event, eventName);

    }

    @Override
    public String cursor() {
        return this.cursor;
    }

    @Override
    public String toString() {
        return cursor;
    }
}
