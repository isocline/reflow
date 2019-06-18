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

import isocline.reflow.WorkEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 *
 */
public class SimultaneousEventSet  {

    private final String eventSetName;

    private final String eventKeyNameForEventSet;

    private int sumOfHashcode = 0;


    /**
     * SimultaneousEventSet is bundle of event names.
     * ex)  event1&event2
     *
     * @param eventSetName
     */
    public SimultaneousEventSet(String eventSetName) {
        this.eventSetName = eventSetName;
        this.eventKeyNameForEventSet = WorkEventKey.PREFIX_EVENT_SET+ eventSetName;

        String[] eventNames = eventSetName.split("&");

        for (String eventName : eventNames) {
            sumOfHashcode = sumOfHashcode + eventName.hashCode();
        }

    }


    /**
     * Returns a EventSetName
     *
     * @return
     */
    public String getEventSetName() {
        return this.eventSetName;
    }


    /**
     * Check
     *
     * @param eventName
     * @return
     */
    public boolean isRaiseEventReady(WorkEvent event, String eventName) {

        int hashCode = eventName.hashCode();


        AtomicInteger integer = event.origin().getCounter(this.eventKeyNameForEventSet);

        if (integer.get() == this.sumOfHashcode) {
            return false;
        }
        int sum = integer.addAndGet(hashCode);


        return sum == this.sumOfHashcode;


    }
}