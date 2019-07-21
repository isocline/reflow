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

/**
 * Indicates that an activity can be controlled.
 * The <tt>Activity</tt> interface provides synchronized execution by the block method.
 *
 * @see Plan
 */
public interface Activity {


    /**
     * Inactive this activity.
     */
    void inactive();


    /**
     * Wait until the activity process is complete.
     *
     * @return an instance of Activity
     */
    Activity block();

    /**
     * @param timeout milli seconds for timeout
     * @return an instance of Activity
     */
    Activity block(long timeout);

    /**
     * Returns Throwable object if an error exists
     *
     * @return Throwable
     */
    Throwable getError();

    /**
     * Sets a Throwable error object
     *
     * @param error the error (which is saved for later retrieval by the getError() method)
     */
    void setError(Throwable error);

    /**
     * Returns a Workflow instance which generates this Activity
     *
     * @return an instance of WorkFlow
     */
    WorkFlow getWorkFlow();


    /**
     * Emits a WorkEvent which is valid only in the local scope
     *
     * @param event WorkEvent object
     * @return an instance of WorkFlow
     */
    Activity emit(WorkEvent event);

    /**
     * Emits a WorkEvent which is valid only in the local scope
     *
     * @param event WorkEvent object
     * @param delayTime delay time
     * @return an instance of WorkFlow
     */
    Activity emit(WorkEvent event, long delayTime);


    /**
     * Returns a FlowProcessor object.
     *
     * @return an instance of FlowProcessor
     */
    FlowProcessor getFlowProcessor();


    /**
     * Finish an Activity at a user-defined time.
     *
     * @param isoDateTime this date-time as a String, such as 2019-06-16T10:15:30Z or 2019-06-16T10:15:30+01:00[Europe/Paris].
     * @return an instance of WorkFlow
     * @throws java.text.ParseException
     */
    Activity finish(String isoDateTime) throws java.text.ParseException;

    /**
     * Finish an Activity at a user-defined time.
     *
     * @param endDateTime Date for end time
     * @return an instance of WorkFlow
     */
    Activity finish(Date endDateTime);


    /**s
     * Finish an Activity at a user-defined time.
     *
     * @param milliSeconds
     * @return an instance of WorkFlow
     */
    Activity finishFromNow(long milliSeconds);

    /**
     * Returns an execute interval time
     *
     * @return interval time
     */
    long getIntervalTime();

}
