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
 *
 * @see Plan
 */
public interface Activity {



    void inactive();

    Activity block();

    Activity block(long timeout);

    Throwable getError();

    void setError(Throwable error);

    WorkFlow getWorkFlow();

    Activity emit(WorkEvent event);

    Activity emit(WorkEvent event, long delayTime);


    FlowProcessor getFlowProcessor();


    Activity finish(String isoDateTime) throws java.text.ParseException;

    Activity finish(Date endDateTime);

    Activity finishFromNow(long milliSeconds);

    long getIntervalTime();

    long getNextExecDelayTime();


    }
