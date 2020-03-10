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
import isocline.reflow.log.XLogger;


/**
 *
 */
public class WorkEventGenerator implements Work {


    private static final XLogger logger = XLogger.getLogger(WorkEventGenerator.class);

    private long intervalTime = Work.WAIT;

    private int count = 0;

    private String eventName = "Reflow:signal";

    public WorkEventGenerator() {

    }

    public WorkEventGenerator(String eventName, long intervalTime) {
        this.eventName = eventName;
        this.intervalTime = intervalTime;
    }


    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setIntervalTime(long intervalTime) {
        this.intervalTime = intervalTime;
    }

    @Override
    public long execute(WorkEvent event) throws InterruptedException {

        count++;

        WorkEvent newEvent = event.createChild(eventName);
        //WorkEvent newEvent = WorkEventFactory.createOrigin(eventName);


        event.getActivity().getFlowProcessor().emit(eventName, newEvent);

        //event.getActivity().getFlowProcessor().emit(eventName, event);
        //FlowProcessorFactory.getProcessor().emit(eventName, newEvent);

        logger.debug("fire event [" + eventName+"] nexttime:"+ intervalTime);

        return intervalTime;
    }

    public int getCount() {
        return this.count;
    }
}
