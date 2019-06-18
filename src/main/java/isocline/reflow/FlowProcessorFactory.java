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

import java.util.HashMap;
import java.util.Map;


/**
 * Creates and returns a default FlowProcessor implementation object.
 * FlowProcessor is the core engine of work execution, and FlowProcessorFactory determines,
 * manages and creates the characteristics of FlowProcessor.
 *
 * @author Richard D. Kim
 */
public class FlowProcessorFactory {

    private static FlowProcessor flowProcessor;

    private static Map<String, FlowProcessor> processorMap = new HashMap<>();


    /**
     * Returns the underlying FlowProcessor implementation object.
     *
     * @return FlowProcessor
     */
    public static FlowProcessor getProcessor() {


        if (flowProcessor == null || !flowProcessor.isWorking()) {
            flowProcessor = new FlowProcessor("default", getDefaultConfiguration());
        }

        return flowProcessor;
    }

    /**
     *
     * @deprecated
     * @return an instance of FlowProcessor
     */
    public static FlowProcessor getDefaultProcessor() {
        return getProcessor();
    }

    private static Configuration getDefaultConfiguration() {
        String processorType = System.getProperty("isocline.Reflow.processor.type");

        if ("performance".equals(processorType)) {
            return Configuration.PERFORMANCE;
        } else if ("echo".equals(processorType)) {
            return Configuration.ECHO;
        } else if ("hyper".equals(processorType)) {
            return Configuration.HYPER;
        }

        return Configuration.NOMAL;
    }


    /**
     * Returns a customized FlowProcessor implementation object.
     *
     * @param id a unique ID for FlowProcessor
     * @param config an instance of Configuration
     * @return a new instance of FlowProcessor
     */
    public static synchronized FlowProcessor getProcessor(String id, Configuration config) {
        FlowProcessor processor = processorMap.get(id);
        if (processor == null || !processor.isWorking()) {
            processor = new FlowProcessor(id, config);
            processorMap.put(id, processor);
        }

        return processor;

    }


}
