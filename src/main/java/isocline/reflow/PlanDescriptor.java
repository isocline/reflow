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


/**
 * An interface to implement the function to pass schedule setting status information to WorkScheduler
 *
 *
 * @see Plan
 * @see isocline.reflow.descriptor.CronDescriptor
 */
public interface PlanDescriptor {


    /**
     * Pass the configuration information to the Plan.
     *
     * @param planning Plan
     */
    void build(Plan planning);

}
