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


/**
 *
 * Factory class for WorkEvent object creation.
 *
 * @author Richard D. Kim
 */
public class WorkEventFactory {



    public static WorkEvent create(String eventName, WorkEvent originEvent) {

        WorkEventImpl event = (WorkEventImpl) originEvent;
        event.setOriginWorkEvent(originEvent);
        event.setEventName(eventName);

        return event;
    }


    /**
     *
     * Create a implement object of WorkEvent
     *
     * @param eventName
     * @return
     */
    public static WorkEvent createOrigin(String eventName) {
        WorkEventImpl event = new WorkEventImpl(eventName);

        return event;
    }



    /**
     * Create a implement object of WorkEvent
     *
     * @return
     */
    public static WorkEvent createOrigin() {
        WorkEventImpl event = new WorkEventImpl();


        return event;
    }
}
