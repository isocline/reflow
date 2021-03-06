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

import isocline.reflow.FlowProcessException;
import isocline.reflow.FunctionExecFeature;
import isocline.reflow.WorkEvent;
import isocline.reflow.WorkHelper;
import isocline.reflow.event.WorkEventImpl;
import isocline.reflow.event.WorkEventKey;
import isocline.reflow.flow.func.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 *
 *
 */
public class FunctionExecutor<T, R> implements FunctionExecFeature {

    private static short nonce = -1;

    private boolean isLastExecutor = false;


    private String recvEventName;

    private String fireEventUUID;

    private String fireEventName;

    private String[] sucessFireEventNames = null;

    private String[] failFireEventNames = new String[1];

    private String[] beforeFireEventNames = null;

    private String[] endFireEventNames = null;

    private String[] timeoutFireEventNames = null;

    private long timeout = 0;


    private long delayTimeFireEvent = 0;


    private int maxCallCount = 0;


    private Runnable runnable = null;

    private Consumer<java.util.Map> consumer = null;

    private Supplier supplier = null;


    private Function<? super T, ? extends R> function = null;


    private ThrowableRunFunction throwableRunFunction = null;

    private WorkEventConsumer workEventConsumer = null;

    private WorkEventFunction workEventFunction = null;


    private CheckFunction checkFunction = null;

    private ReturnEventFunction returnEventFunction = null;


    public FunctionExecutor() {
        this.fireEventUUID = getUUID();

        addFailFireEventName(WorkEventKey.PREFIX_ERROR + this.fireEventUUID);
    }


    public FunctionExecutor(Object obj) {

        if (obj != null) {
            if (obj instanceof Runnable) {
                this.runnable = (Runnable) obj;
            } else if (obj instanceof Consumer) {

                this.consumer = (Consumer) obj;

            } else if (obj instanceof WorkEventConsumer) {
                this.workEventConsumer = (WorkEventConsumer) obj;
            } else if (obj instanceof Supplier) {
                this.supplier = (Supplier) obj;
            } else if (obj instanceof Function) {
                this.function = (Function) obj;
            } else if (obj instanceof CheckFunction) {
                this.checkFunction = (CheckFunction) obj;
            } else if (obj instanceof ReturnEventFunction) {
                this.returnEventFunction = (ReturnEventFunction) obj;
            } else if (obj instanceof WorkEventFunction) {
                this.workEventFunction = (WorkEventFunction) obj;
            } else if (obj instanceof ThrowableRunFunction) {
                this.throwableRunFunction = (ThrowableRunFunction) obj;
            }  else {
                throw new IllegalArgumentException("Not Support type");
            }
        }


        this.fireEventUUID = getUUID();
        addFailFireEventName(WorkEventKey.PREFIX_ERROR + this.fireEventUUID);
    }

    private String getUUID() {
        nonce++;
        return WorkEventKey.PREFIX_FUNC_UUID + nonce + "x" + String.valueOf(this.hashCode());
    }

    public String getFireEventUUID() {
        return this.fireEventUUID;
    }

    public void setLastExecutor(boolean isEnd) {
        this.isLastExecutor = isEnd;
    }

    public boolean isLastExecutor() {
        return isLastExecutor;
    }


    public void setFireEventName(String eventName) {
        this.fireEventName = eventName;


        addFailFireEventName(WorkEventKey.PREFIX_ERROR + eventName);


    }

    private void addFailFireEventName(String... failEventName) {
        if (failFireEventNames == null) {
            failFireEventNames = failEventName;

        } else {
            final int oldSize = failFireEventNames.length;
            final int newSize = failFireEventNames.length + failEventName.length;
            String[] newFailFireEventNames = new String[newSize];

            System.arraycopy(failFireEventNames, 0, newFailFireEventNames, 0, failFireEventNames.length);
            System.arraycopy(failEventName, 0, newFailFireEventNames, oldSize, failEventName.length);

            failFireEventNames = newFailFireEventNames;
        }
    }

    public void setRecvEventName(String eventName) {
        this.recvEventName = eventName;
    }


    public String getFireEventName() {
        return this.fireEventName;
    }

    public String getRecvEventName() {
        return this.recvEventName;
    }

    public long getDelayTimeFireEvent() {
        return delayTimeFireEvent;
    }

    public void setDelayTimeFireEvent(long delayTimeFireEvent) {
        this.delayTimeFireEvent = delayTimeFireEvent;
    }


    public int getMaxCallCount() {
        return maxCallCount;
    }

    public void setMaxCallCount(int maxCallCount) {
        this.maxCallCount = maxCallCount;
    }


    private AtomicInteger getCallCounter(WorkEvent event) {

        WorkEvent origin = event.origin();

        String countKeyName = this.fireEventUUID + "<count>";

        return origin.getCounter(countKeyName);
    }


    public ResultState execute(WorkEvent event) throws Throwable {

        String pFireEventName = this.fireEventName;
        String pFireEventUUID = this.fireEventUUID;

        boolean isProcessNext = true;

        AtomicInteger counter = getCallCounter(event);
        int cnt = counter.addAndGet(1);


        if (maxCallCount > 0 && maxCallCount <= cnt) {
            new ResultState(pFireEventUUID, pFireEventName, false);
        }

        WorkEventImpl e = (WorkEventImpl) event;
        e.setEmitCount(cnt);

        if (runnable != null) {
            runnable.run();
        } else if (consumer != null) {

            WorkEventImpl rootEvent = (WorkEventImpl) event.origin();


            consumer.accept(rootEvent.getAttributeMap());
        } else if (workEventConsumer != null) {
            workEventConsumer.accept(event);
        } else if (workEventFunction != null) {
            Object result = workEventFunction.apply(event);
            WorkHelper.Return(event, result);
        } else if (checkFunction != null) {
            boolean runNext = checkFunction.check(event);
            if (!runNext) {
                isLastExecutor = true;
            }

            isProcessNext = runNext;
        } else if (returnEventFunction != null) {
            String newEventName = returnEventFunction.checkFlow(event);
            if (newEventName != null) {
                pFireEventName = null;
                pFireEventUUID = newEventName;
            }


        } else if (throwableRunFunction != null) {
            throwableRunFunction.run();
        } else if (function != null) {
            T t = (T) WorkHelper.Get(event);
            R r = function.apply(t);

            WorkHelper.Return(event, r);
        }


        return new ResultState(pFireEventUUID, pFireEventName, isProcessNext);


    }


    @Override
    public FunctionExecFeature before(String... eventNames) {
        if (this.beforeFireEventNames != null) {
            throw new IllegalStateException("The duplicate method flow is not prohibited");
        }
        this.beforeFireEventNames = eventNames;
        return this;
    }

    @Override
    public FunctionExecFeature success(String... eventNames) {
        if (this.sucessFireEventNames != null) {
            throw new IllegalStateException("The duplicate method flow is not prohibited");
        }
        this.sucessFireEventNames = eventNames;
        return this;
    }

    @Override
    public FunctionExecFeature fail(String... eventNames) {
        addFailFireEventName(eventNames);
        return this;
    }

    @Override
    public FunctionExecFeature timeout(long timeout, String... eventNames) {

        this.timeout = timeout;
        this.timeoutFireEventNames = eventNames;
        return this;
    }

    @Override
    public FunctionExecFeature end(String... eventNames) {
        if (this.endFireEventNames != null) {
            throw new IllegalStateException("The duplicate method flow is not prohibited");
        }
        this.endFireEventNames = eventNames;
        return this;
    }

    private CircuitBreakInfo circuitBreakInfo;

    @Override
    public FunctionExecFeature circuitBreak(String id, int maxFailCount, long recoveryWaitTime) {

        this.circuitBreakInfo = circuitBreakInfoMap.computeIfAbsent(id, k -> {
            CircuitBreakInfo newInfo = new CircuitBreakInfo();
            newInfo.maxFailCount = maxFailCount;
            newInfo.recoveryWaitTime = recoveryWaitTime;

            return newInfo;
        });

        return this;
    }

    public void onFail() {
        if (circuitBreakInfo != null) {
            circuitBreakInfo.onFail();
        }
    }

    public void onSuccess() {
        if (circuitBreakInfo != null) {
            circuitBreakInfo.onSuccess();
        }

    }

    public void check() {
        if (circuitBreakInfo != null) {
            circuitBreakInfo.check();
        }


    }

    public long getTimeout() {
        return this.timeout;
    }

    public String[] getBeforeFireEventNames() {
        return this.beforeFireEventNames;
    }

    public String[] getSucessFireEventNames() {
        return this.sucessFireEventNames;
    }

    public String[] getFailFireEventNames() {
        return this.failFireEventNames;
    }

    public String[] getEndFireEventNames() {
        return this.endFireEventNames;
    }


    public String[] getTimeoutFireEventNames() {
        return this.timeoutFireEventNames;
    }


    final public static class ResultState {

        private final String fireEventUUID;
        private final String fireEventName;
        private final boolean isProcessNext;

        ResultState(String fireEventUUID, String fireEventName, boolean isProcessNext) {
            this.fireEventUUID = fireEventUUID;
            this.fireEventName = fireEventName;
            this.isProcessNext = isProcessNext;
        }

        public String getFireEventUUID() {
            return fireEventUUID;
        }


        public String getFireEventName() {
            return fireEventName;
        }

        public boolean isProcessNext() {
            return this.isProcessNext;
        }

    }

    private static final Map<String, CircuitBreakInfo> circuitBreakInfoMap = new HashMap<>();

    final private static class CircuitBreakInfo {
        private int maxFailCount;
        private long recoveryWaitTime;
        private long lastFailTime = 0;
        private final AtomicInteger failCounter = new AtomicInteger(0);

        private boolean checkFail = true;

        private void onFail() {

            if(checkFail) {
                int s = this.failCounter.addAndGet(1);
            }
        }

        private void onSuccess() {
            this.failCounter.set(0);
        }

        void check() {
            //System.out.println("CHK  "+failCounter.get() + " "+maxFailCount);
            if (failCounter.get() > maxFailCount) {

                if (lastFailTime > 0) {
                    long t1 = System.currentTimeMillis();
                    long t2 = lastFailTime + recoveryWaitTime;

                    if (t1 > t2) {
                        lastFailTime = 0;
                        return;
                    }else {
                        checkFail = false;
                        throw new FlowProcessException("Circuit Breaker");
                    }
                } else {
                    lastFailTime = System.currentTimeMillis();
                    checkFail = false;
                    throw new FlowProcessException("Circuit Breaker");

                }


            }

            checkFail = true;
        }
    }
}
