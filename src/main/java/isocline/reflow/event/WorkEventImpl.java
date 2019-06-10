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

import isocline.reflow.Plan;
import isocline.reflow.WorkEvent;

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

    private Map<String,AtomicInteger> counterMap = new HashMap<>();

    private Plan schedule;

    private WorkEvent originWorkEvent;

    private Throwable throwable;


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

    public WorkEventImpl setEventName(String eventName) {
        this.eventName = eventName;
        return this;
    }

    public String getEventName() {
        return this.eventName;
    }


    public void setPlan(Plan sechedule) {
        this.schedule = sechedule;
    }


    /**
     * @return
     */
    public Plan getPlan() {

        return this.schedule;
    }


    /**
     * @param key
     * @param value
     */
    public void setAttribute(String key, Object value) {
        this.attributeMap.put(key, value);

    }

    /**
     * @param key
     * @return
     */
    public Object getAttribute(String key) {
        return this.attributeMap.get(key);
    }


    @Override
    public synchronized AtomicInteger getCounter(String key) {
        AtomicInteger counter = counterMap.get(key);
        if(counter==null) {
            counter = new AtomicInteger(0);
            counterMap.put(key, counter);
        }

        return counter;
    }

    /**
     * @param key
     * @return
     */
    public Object removeAttribute(String key) {
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
        event2.schedule = this.schedule;
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
        //newEvent.attributeMap = this.attributeMap;
        newEvent.schedule = this.schedule;


        return newEvent;


    }


    @Override
    public void setFireTime(long time) {
        this.fireTime = time;
    }

    @Override
    public long getFireTime() {
        return this.fireTime;
    }

    @Override
    public void setThrowable(Throwable e) {
        this.throwable = e;

    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public WorkEvent origin() {
        return this.originWorkEvent;
    }

    @Override
    public String getFireEventName() {
        if (this.fireEventName != null) {
            return this.fireEventName;
        }
        return eventName;
    }

    @Override
    public void setFireEventName(String eventName) {
        this.fireEventName = eventName;

    }

    @Override
    public Stream getStream() {
        WorkEvent event = this.origin();
        if (event == null) {
            event = this;
        }

        String resultKey = "result::" + event.hashCode();

        List list = null;
        synchronized (event) {
            List newList = Collections.synchronizedList(new ArrayList<>());
            ;
            list = (List) event.getAttribute(resultKey);
            if (list == null) {
                list = newList;

                event.setAttribute(resultKey, list);
            } else {
                event.setAttribute(resultKey, newList);
            }
        }

        Stream stream = list.stream();


        return stream;
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

        String resultKey = "result::" + event.hashCode() + "<Mono>";


        return event.getAttribute(resultKey);
    }

    @Override
    public String toString() {
        if(this==this.originWorkEvent) {
            return "WorkEventImpl:origin{" +this.hashCode()+" "+
                    "eventName='" + eventName + '\'' +

                    '}';
        }
        return "WorkEventImpl{" +
                "eventName='" + eventName + '\'' +
                "origin='" + this.originWorkEvent + '\'' +
                '}';
    }
}
