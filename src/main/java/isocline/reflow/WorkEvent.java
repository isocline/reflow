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

import isocline.reflow.flow.func.WorkEventConsumer;
import isocline.reflow.flow.func.WorkEventPredicate;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A interface that represents the event to be delivered when the work object is executed.
 *
 * @author Richard D. Kim
 * @see Work
 * @see FlowableWork
 */
public interface WorkEvent extends ResultEvent {


    /**
     * Returns the event name.
     *
     * @return name of event
     */
    String getEventName();


    int count();

    /**
     * Set the Activity object.
     *
     * @param activity instance of Activity
     */
    void setActivity(Activity activity);


    /**
     * Returns the Activity object.
     *
     * @return instance of Activity
     */
    Activity getActivity();


    /**
     * Set additional custom attribute values.
     *
     * @param key   the key name to which the object is bound; cannot be null
     * @param value the object to be bound
     * @return an instance of WorkEvent
     */
    WorkEvent put(String key, Object value);



    /**
     * Delete the attribute value.
     *
     * @param key the name of the object to remove from this session
     * @return the name of the object to remove from this session
     */
    Object remove(String key);


    void reset();


    AtomicInteger getCounter(String key);


    /**
     * Copy internal information to another Event object.
     *
     * @param event an instance of WorkEvent that want to copy
     */
    void copyTo(WorkEvent event);


    /**
     * Creates a child object of the object.
     * The child object shares the property value information of the parent object.
     *
     * @param eventName name of event
     * @return an new instance of WorkEvent
     */
    WorkEvent createChild(String eventName);


    /**
     * Returns the earliest event among the events associated with the current event.
     *
     * @return first origin of WorkEvent
     */
    WorkEvent origin();


    /**
     * Set error information.
     *
     * @param e Throwable error
     * @return an instance of WorkEvent
     */
    WorkEvent setThrowable(Throwable e);





    WorkEvent setTimeoutThread(Thread thread);


    Thread getTimeoutThread();


    /**
     * Defines the valid time at which the event occurs.
     *
     * @param time the milliseconds since January 1, 1970, 00:00:00 GMT.
     * @return an instance of WorkEvent
     */
    WorkEvent setFireTime(long time);


    /**
     * Returns the valid time at which the event occurs.
     *
     * @return time the milliseconds since January 1, 1970, 00:00:00 GMT.
     */
    long getFireTime();


    /**
     * Returns name of fire event.
     * @return name of event
     */
    String getFireEventName();


    /**
     * Set name of fire event.
     *
     * @param eventName name of event
     * @return instance of WorkEvent
     */
    WorkEvent setFireEventName(String eventName);


    /**
     * Returns result object for internal process.
     *
     * @return instance of result, if exist. or null
     */
    Object getResult();


    /**
     * Return data stream.
     *
     * @return instance of normal stream.
     */
    Stream getStream();


    /**
     * Returns integer stream, if internal work process produces stream.
     *
     * @return integer stream
     */
    IntStream getIntStream();


    /**
     * Returns long stream, if internal work process produces stream.
     * @return long stream
     */
    LongStream getLongStream();


    /**
     * Returns double stream, if internal work process produces stream.
     *
     * @return double stream.
     */
    DoubleStream getDoubleStream();


    /**
     * Set an instance of WorkEventPredicate for filtering process.
     *
     * @param tester an instance of WorkEventPredicate
     * @return an instance of WorkEvent
     */
    WorkEvent filter(WorkEventPredicate tester);


    /**
     * Set an instance or WorkEventConsumer for receiving result.
     *
     * @param consumer an instance of WorkEventConsumer
     * @return an instance of WorkEvent
     */
    WorkEvent subscribe(WorkEventConsumer consumer);


    /**
     * Set an instance of WorkEvent for callback.
     *
     * @param event an instance of WorkEvent
     * @return true if success setup.
     */
    boolean callback(WorkEvent event);


    /**
     * execute method if you want to ternminate internal work process.
     *
     */
    void complete();




    WorkEvent dataChannel(DataChannel dataChannel);


    boolean publish();


    boolean isComplete();


    Activity propagate(String eventName);



}
