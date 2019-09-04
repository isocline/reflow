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
import isocline.reflow.flow.func.WorkEventPredicate;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;


/**
 * A skeletal {@link WorkEvent} implementation.
 */
public class WorkEventImpl implements WorkEvent {


    private final static int MAX_CALLBACK_EVENT_QUEUE_SIZE = 100;


    private String eventName = null;

    private long fireTime = -1;

    private int emitCount = 0;

    private String fireEventName;



    private boolean isCallBacking = false;



    private final LinkedBlockingQueue<WorkEvent> workEventQueue = new LinkedBlockingQueue<>();

    @SuppressWarnings("unchecked")
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
     * Get instance of Activity.
     *
     * @return instance of Activity
     */
    public Activity getActivity() {

        return this.activity;
    }


    /**
     * Put value with key.
     *
     * @param key key of value
     * @param value value
     */
    public WorkEvent put(String key, Object value) {
        this.attributeMap.put(key, value);

        return this;
    }

    /**
     * Get value by key
     *
     * @param key key of value
     * @return value
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

        return counterMap.computeIfAbsent(key, k -> new AtomicInteger(0));
    }

    /**
     *
     * Remove value by key.
     *
     * @param key key of value
     * @return removed value
     */
    public Object remove(String key) {
        return this.attributeMap.remove(key);
    }


    public Map getAttributeMap() {
        return this.attributeMap;
    }

    /**
     * copy internal properties to another instance of WorkEvent.
     *
     * @param event instance of event
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


    private WorkEventPredicate tester;

    private WorkEventConsumer consumer;

    @Override
    public WorkEvent subscribe(WorkEventConsumer consumer) {

        this.consumer = consumer;
        return this;
    }


    @Override
    public WorkEvent filter(WorkEventPredicate tester) {
        this.tester = tester;
        return this;
    }

    @Override
    public boolean callback(WorkEvent event) {
        if(this.consumer!=null) {


            if(tester!=null) {
                if( !tester.test(event)) {
                    return false;
                }
            }

            if(workEventQueue.size()>MAX_CALLBACK_EVENT_QUEUE_SIZE) {
                workEventQueue.clear();
                throw new RuntimeException("Queue is overflow. size="+MAX_CALLBACK_EVENT_QUEUE_SIZE);
            }

            workEventQueue.add(event);
            if(isCallBacking) {
                return false;
            }

            isCallBacking = true;

            WorkEvent x;
            try {
                int seq=0;
                while ((x = workEventQueue.poll()) != null) {
                    seq++;
                    this.consumer.accept(x);

                }
            }finally {
                isCallBacking = false;
            }

            return true;

        }else {
            return false;
        }
    }

    private boolean isComplete = false;
    @Override
    public synchronized void complete() {

        if(isComplete) {
            return;
        }
        isComplete = true;
        if(this.consumer!=null) {
            consumer.accept(this);
        }

        if(this!=this.originWorkEvent) {
            this.originWorkEvent.complete();
        }
        notifyAll();

    }

    @Override
    public synchronized void block() {
        try {
            if (!this.isComplete) {
                wait();
            }
        } catch (InterruptedException ignored) {

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
