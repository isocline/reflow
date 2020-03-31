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
import isocline.reflow.flow.func.*;

import java.util.function.Function;

public class WorkFlowWrapper<T> extends WorkFlow<Object> {

    private final WorkFlow workFlowInstance;

    private final String cursor;


    protected WorkFlowWrapper() {
        this.workFlowInstance = null;
        this.cursor = null;
    }

    public WorkFlowWrapper(WorkFlow workFlow) {
        super();

        this.workFlowInstance = workFlow;
        this.cursor = workFlow.cursor();

    }


    @Override
    public WorkFlow<Object> wait(String... eventNames) {
        this.workFlowInstance.wait(eventNames);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<Object> waitAll() {
        this.workFlowInstance.waitAll();
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<Object> waitAll(String... eventNames) {
        this.workFlowInstance.waitAll(eventNames);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<Object> onError(String... eventNames) {
        this.workFlowInstance.onError(eventNames);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<Object> wait(WorkFlow... workFlows) {
        this.workFlowInstance.wait(workFlows);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<Object> waitAll(WorkFlow... workFlows) {
        this.workFlowInstance.waitAll(workFlows);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<Object> onError(WorkFlow... workFlows) {
        this.workFlowInstance.onError(workFlows);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<Object> onError(Class... errorClasses) {
        this.workFlowInstance.onError(errorClasses);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<Object> onError(WorkEventConsumer consumer) {
        this.workFlowInstance.onError(consumer);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<Object> runAsync(Runnable... execObject) {
        this.workFlowInstance.runAsync(execObject);
        return new WorkFlowWrapper(this.workFlowInstance);
    }


    @Override
    public WorkFlow<Object> runAsync(Runnable execObject, String eventName) {
        this.workFlowInstance.runAsync(execObject, eventName);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<Object> runAsync(Runnable execObject, int count) {
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
    public WorkFlow<Object> runAsync(WorkEventConsumer... execObject) {
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
    public WorkFlow<T> extractAsync(WorkEventFunction... execObject) {
        this.workFlowInstance.extractAsync(execObject);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> extractAsync(WorkEventFunction execObject, String fireEventName) {
        this.workFlowInstance.extractAsync(execObject, fireEventName);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<T> extractAsync(WorkEventFunction execObject, int count) {
        this.workFlowInstance.extractAsync(execObject, count);
        return new WorkFlowWrapper(this.workFlowInstance);
    }


    @SafeVarargs
    @Override
    public final <R> WorkFlow<R> extract(WorkEventFunction<? extends R>... execObjects) {
        this.workFlowInstance.extract(execObjects);
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

/*
    @Override
    public WorkFlow<T> next(Consumer<? super T> execObject) {
        this.workFlowInstance.next(execObject);
        return new WorkFlowWrapper(this.workFlowInstance);
    }


    @Override
    public WorkFlow<T> next(Consumer<? super T> execObject, String eventName) {
        this.workFlowInstance.next(execObject, eventName);
        return new WorkFlowWrapper(this.workFlowInstance);
    }



    @Override
    public WorkFlow<T> next(Consumer<? super T> execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        this.workFlowInstance.next(execObject, fnExecFeatureFunction);
        return new WorkFlowWrapper(this.workFlowInstance);
    }
*/

    @Override
    public WorkFlow<Object> next(WorkEventConsumer execObject) {
        this.workFlowInstance.next(execObject);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<Object> next(WorkEventConsumer execObject, String eventName) {
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
    public WorkFlow<Object> next(ThrowableRunFunction execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        this.workFlowInstance.next(execObject, fnExecFeatureFunction);
        return new WorkFlowWrapper(this.workFlowInstance);
    }

    @Override
    public WorkFlow<Object> next(WorkEventConsumer execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        this.workFlowInstance.next(execObject, fnExecFeatureFunction);
        return new WorkFlowWrapper(this.workFlowInstance);
    }


    @Override
    public <R> WorkFlow<R> next(WorkEventFunction<? extends R> execObject, FnExecFeatureFunction fnExecFeatureFunction) {
        this.workFlowInstance.next(execObject, fnExecFeatureFunction);
        return new WorkFlowWrapper(this.workFlowInstance);
    }


    @Override
    public <R> WorkFlow<R> trans(Function<? super Object, ? extends R> mapper) {
        this.workFlowInstance.trans(mapper);
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
