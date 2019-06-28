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

public interface Plan {


    String getId();

    void lock(Object lockOwner) throws IllegalAccessException;

    void unlock(Object lockOwner) throws IllegalAccessException;


    Plan startTime(long nextExecuteTime);

    Plan initialDelay(long waitTime);

    Plan interval(long intervalTime);

    Plan interval(long initialDelay, long intervalTime);

    Plan startTime(String isoDateTime) throws java.text.ParseException;

    Plan startTime(Date startDateTime);

    Plan finishTime(String isoDateTime) throws java.text.ParseException;

    Plan finishTime(Date endDateTime);

    Plan finishTimeFromNow(long milliSeconds);

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



