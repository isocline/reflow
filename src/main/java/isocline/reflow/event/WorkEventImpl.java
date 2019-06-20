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
package isocline.reflow.event;

import isocline.reflow.Activity;
import isocline.reflow.WorkEvent;
import isocline.reflow.flow.func.WorkEventConsumer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;


/**
 * A skeletal {@link WorkEvent} implementation.
 */
public class WorkEventImpl implements WorkEvent {


    private String eventName = null;

    private long fireTime = -1;

    private int emitCount = 0;

    private String fireEventName;


    private Map<String, Object> attributeMap = new Hashtable();

    private final Map<String,AtomicInteger> counterMap = new HashMap<>();

    private Activity activity;

    private WorkEvent originWorkEvent;

    private Throwable throwable;

    private Thread timeoutThread = null;

    WorkEventImpl() {
        this.originWorkEvent = this;
    }

    /**
     * @param eventName
     */
    WorkEventImpl(String eventName) {
        this.eventName = eventName;
        this.originWorkEvent = this;
    }

    /**
     * @param eventName
     * @param originWorkEvent
     */
    WorkEventImpl(String eventName, WorkEvent originWorkEvent) {
        this.eventName = eventName;
        this.originWorkEvent = originWorkEvent;
        /*
        try {
            throw new RuntimeException("xxx");
        }catch (Exception e) {
            e.printStackTrace();
        }
        */
    }


    @Override
    public int count() {
        return this.emitCount;
    }

    public void setEmitCount(int emitCount) {
        this.emitCount = emitCount;
    }

    /*
    public WorkEventImpl setEventName(String eventName) {
        this.eventName = eventName;
        return this;
    }
    */

    public String getEventName() {
        return this.eventName;
    }


    public void setActivity(Activity plan) {
        this.activity = plan;
    }


    /**
     * @return
     */
    public Activity getActivity() {

        return this.activity;
    }


    /**
     * @param key
     * @param value
     */
    public WorkEvent put(String key, Object value) {
        this.attributeMap.put(key, value);

        return this;
    }

    /**
     * @param key
     * @return
     */
    public Object get(String key) {
        return this.attributeMap.get(key);
    }

    @Override
    public void reset() {

        this.attributeMap.clear();
        this.counterMap.clear();

        fireEventName=null;
        originWorkEvent = null;

    }

    @Override
    public synchronized AtomicInteger getCounter(String key) {
        AtomicInteger counter = counterMap.computeIfAbsent(key, k -> new AtomicInteger(0));

        return counter;
    }

    /**
     * @param key
     * @return
     */
    public Object remove(String key) {
        return this.attributeMap.remove(key);
    }


    public Map getAttributeMap() {
        return this.attributeMap;
    }

    /**
     * @param event
     */
    public void copyTo(WorkEvent event) {
        WorkEventImpl event2 = (WorkEventImpl) event;
        event2.activity = this.activity;
        event2.attributeMap = this.attributeMap;
    }


    /**
     * Creates a new {@link WorkEvent} that has parent property information.
     *
     * @param eventName the name of the event to createOrigin; may not be empty
     * @return the newly created WorkEvent
     */
    public WorkEvent createChild(String eventName) {

        if (eventName == null || eventName.trim().length() == 0) {
            throw new IllegalArgumentException("name is empty");
        }


        WorkEventImpl newEvent = new WorkEventImpl(eventName, this.originWorkEvent);
        newEvent.attributeMap = this.attributeMap;
        newEvent.activity = this.activity;



        return newEvent;


    }


    @Override
    public WorkEvent setFireTime(long time) {
        this.fireTime = time;
        return this;
    }

    @Override
    public long getFireTime() {
        return this.fireTime;
    }

    @Override
    public WorkEvent setThrowable(Throwable e) {
        this.throwable = e;
        return this;

    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }


    @Override
    public WorkEvent setTimeoutThread(Thread thread) {
        this.timeoutThread = thread;
        return this;
    }

    @Override
    public Thread getTimeoutThread() {
        return this.timeoutThread;
    }

    @Override
    public WorkEvent origin() {
        return this.originWorkEvent;
    }

    /*
    void setOriginWorkEvent(WorkEvent originWorkEvent) {
        this.originWorkEvent = originWorkEvent;
    }
    */

    @Override
    public String getFireEventName() {
        if (this.fireEventName != null) {
            return this.fireEventName;
        }
        return eventName;
    }

    @Override
    public WorkEvent setFireEventName(String eventName) {
        this.fireEventName = eventName;
        return this;

    }

    @Override
    public Stream getStream() {
        WorkEvent event = this.origin();
        if (event == null) {
            event = this;
        }

        String resultKey = WorkEventKey.PREFIX_RESULT + event.hashCode();

        List list;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (event) {
            List newList = Collections.synchronizedList(new ArrayList<>());

            list = (List) event.get(resultKey);
            if (list == null) {
                list = newList;

                event.put(resultKey, list);
            } else {
                event.put(resultKey, newList);
            }
        }


        return list.stream();
    }


    public IntStream getIntStream() {
        Stream<Integer> stream = getStream();
        return stream.mapToInt(i -> i);
    }


    public LongStream getLongStream() {
        Stream<Integer> stream = getStream();
        return stream.mapToLong(i -> i);
    }

    public DoubleStream getDoubleStream() {
        Stream<Double> stream = getStream();
        return stream.mapToDouble(i -> i);
    }

    @Override
    public Object getResult() {
        WorkEvent event = this.origin();
        if (event == null) {
            event = this;
        }

        String resultKey = WorkEventKey.PREFIX_RESULT + event.hashCode() + "<Mono>";

        return event.get(resultKey);
    }


    private WorkEventConsumer consumer;

    @Override
    public WorkEvent subscribe(WorkEventConsumer consumer) {

        this.consumer = consumer;
        return this;
    }


    private boolean isComplete = false;
    @Override
    public synchronized void complete() {
        if(this.consumer!=null) {
            consumer.accept(this);
        }
        isComplete = true;
        notifyAll();

    }

    @Override
    public synchronized void block() {
        try {
            if (!this.isComplete) {
                wait();
            }
        } catch (InterruptedException ie) {

        }
    }

    @Override
    public String toString() {
        String fireInfo="";
        if(this.fireEventName!=null) {
            fireInfo = "fire:'" + fireEventName + "', ";
        }

        if(this==this.originWorkEvent) {
            return "{" +
                    "hid:'" + hashCode() + "', " +
                    fireInfo+
                    "name:'" + eventName + "'" +
                    '}';
        }



        return "WorkEventImpl:{" +

                "hid:'" + hashCode() + "', " +
                fireInfo +
                "name:'" + eventName + "', " +
                "origin: " + this.originWorkEvent  +
                '}';
    }
}
