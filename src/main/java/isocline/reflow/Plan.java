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

import java.util.Date;
import java.util.function.Consumer;


/**
 * Plan class for process flow.
 * It is responsible for planning the processing time, such as
 * setting the start time and ending time for method execution
 * or process flow execution.
 *
 */
public interface Plan {


    /**
     * Returns a ID of this Plan instance.
     *
     * @return ID of this Plan.
     */
    String getId();

    /**
     * Lock this plan object against modifying parameters of this Plan instance.
     *
     * @param lockOwner an owner instance to lock this Plan instance
     * @throws IllegalAccessException IllegalAccessException
     */
    void lock(Object lockOwner) throws IllegalAccessException;


    /**
     * Unlock this plan object to release locking for modifying parameters of this Plan instance.
     *
     * @param lockOwner an owner instance to lock this Plan instance
     * @throws IllegalAccessException IllegalAccessException
     */
    void unlock(Object lockOwner) throws IllegalAccessException;


    Plan startTime(long nextExecuteTime);

    Plan initialDelay(long waitTime);

    Plan interval(long intervalTime);

    Plan interval(long intervalTime, long initialDelay);

    Plan startTime(String isoDateTime) throws java.text.ParseException;

    Plan startTime(Date startDateTime);

    /**
     * Set the finish time at which the Plan should quit repeating (and be automatically deleted).
     *
     * @param isoDateTime this date-time as a String, such as 2019-06-16T10:15:30Z or 2019-06-16T10:15:30+01:00[Europe/Paris].
     * @return an instance of Plan
     * @throws java.text.ParseException If isoDataTime is not correct format
     */
    Plan finishTime(String isoDateTime) throws java.text.ParseException;

    /**
     * Set the finish time at which the Plan should quit repeating (and be automatically deleted).
     *
     * @param endDateTime this date-time
     * @return an instance of Plan
     */
    Plan finishTime(Date endDateTime);


    /**
     * Sets the time to end from the current time.
     *
     * @param milliSeconds Milli seconds from current time
     * @return an instance of Plan
     */
    Plan finishTimeFromNow(long milliSeconds);

    /**
     * Sets the time to finish from the start time.
     *
     * @param milliSeconds Milli seconds from the start time
     * @return an instance of Plan
     */
    Plan finishTimeFromStart(long milliSeconds);



    Plan on(Object... eventNames);


    Plan setBetweenStartTimeMode(boolean isBetweenStartTimeMode);

    Plan jitter(long jitter);


    Plan strictMode();

    Plan daemonMode();

    boolean isDaemonMode() ;


    Plan eventChecker(ExecuteEventChecker checker);


    Plan describe(PlanDescriptor descriptor);

    Activity activate();

    <T> Activity activate(Consumer<T> consumer);

    Activity run();

    boolean isActivated();


    WorkFlow getWorkFlow();
}



