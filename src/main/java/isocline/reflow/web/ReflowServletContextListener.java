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
package isocline.reflow.web;

import isocline.reflow.FlowProcessor;
import isocline.reflow.FlowProcessorFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;


/**
 *
 *
 */
@WebListener
public class ReflowServletContextListener implements
        javax.servlet.ServletContextListener {

    private FlowProcessor processor;

    /**
     * Initialize a Context of Servlet
     *
     * @param sce an instance of ServletContextEvent
     */
    public void contextInitialized(ServletContextEvent sce) {
        processor = FlowProcessorFactory.getProcessor();

    }

    /**
     * Destroyed context of Servlet
     *
     * @param sce an instance of ServletContextEvent
     * @see javax.servlet.ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce) {
        processor.shutdown(3000);

    }

}
