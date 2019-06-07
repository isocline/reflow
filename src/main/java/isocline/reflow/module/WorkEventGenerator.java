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
package isocline.reflow.module;

import isocline.reflow.Work;
import isocline.reflow.WorkEvent;


/**
 *
 */
public class WorkEventGenerator implements Work {


    private long timeGap = 1000;

    private String eventName = "reflow:signal";


    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setRepeatTime(long repeatTime) {
        this.timeGap = repeatTime;
    }

    @Override
    public long execute(WorkEvent event) throws InterruptedException {


        WorkEvent newEvent = event.createChild(eventName);


        event.getPlan().getWorkProcessor().raiseEvent(eventName, newEvent);

        //event.getPlan().getWorkProcessor().raiseEvent(eventName, event);
        //WorkProcessorFactory.getProcessor().raiseEvent(eventName, newEvent);

        System.out.println("fire event [" + eventName+"]");

        return timeGap;
    }
}
