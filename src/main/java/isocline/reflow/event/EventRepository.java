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


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 * @param <K>
 * @param <V>
 */
public class EventRepository<K,V> extends HashMap<K,V> {

    private Map<String, SimultaneousEventSet> eventMap = new ConcurrentHashMap<>();


    public String[] setBindEventNames(String eventNameMeta) {


        String[] eventNames = eventNameMeta.split("&");

        if(eventNames.length>1) {

            SimultaneousEventSet simultaneousEventSet = new SimultaneousEventSet(eventNameMeta);

            /*
            for (String eventName : eventNames) {
                simultaneousEventSet.add(eventName);
            }
            */
            for (String eventName : eventNames) {
                eventMap.put(eventName, simultaneousEventSet);
            }
        }

        return eventNames;
    }

    public SimultaneousEventSet getSimultaneousEventSet(String eventName) {
        SimultaneousEventSet simultaneousEventSet = eventMap.get(eventName);

        return simultaneousEventSet;
    }
}
