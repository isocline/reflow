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


import isocline.reflow.event.WorkEventKey;
import isocline.reflow.flow.FunctionExecutor;
import isocline.reflow.flow.FunctionExecutorList;

/**
 * It is an interface that enables flow control.
 * If you want to control flow with several methods in an object that implements this interface, you can inherit the interface.
 *
 * @see isocline.reflow.Work
 */
public interface FlowableWork<T> extends Work {


    /**
     * initialize
     *
     * @param workEvent an instance of WorkEvent
     */
    default void initialize(WorkEvent workEvent) {

    }


    /**
     * It is a method that must be implemented in order to do flow control.
     * <p>
     * <strong>Example:</strong>
     * <blockquote>
     * <pre>
     *
     *  public void defineWorkFlow(WorkFlow flow) {
     *    // step1 : activate this.checkMemory() then activate this.checkStorage()
     *    WorkFlow p1 = flow.next(this::checkMemory).next(this::checkStorage);
     *
     *    // Until wait inactive of step1, then this.sendSignal()
     *    WorkFlow t1 = flow.wait(p1).next(this::sendSignal);
     *
     *    // Until wait inactive of step1, then this.sendStatusMsg() and this.sendReportMsg()
     *    WorkFlow t2 = flow.wait(p1).next(this::sendStatusMsg).next(this::sendReportMsg);
     *
     *    // Wait until both step1 and step2 are finished, then activate this.report()
     *    flow.waitAll(t1, t2).next(this::report).inactive();
     *  }
     * </pre>
     * </blockquote>
     *
     * @param flow WorkFlow instance
     */
    void defineWorkFlow(WorkFlow<T> flow);


    /**
     * It is not necessary to implement additional methods as extended methods implemented from the Work interface.
     * DO NOT implement this method.
     *
     * @return delay time
     * @throws InterruptedException If interrupt occur
     */
    default long execute(WorkEvent event) throws InterruptedException {


        final ActivatedPlan plan = event.getPlan();

        final WorkFlow flow = plan.getWorkFlow();

        if (flow == null) {
            return TERMINATE;
        }

        final String eventName = event.getFireEventName();

        System.out.println(Thread.currentThread().getId()+" RECV "+eventName + " "+event.origin().getCounter(WorkEventKey.COUNTER_FUNC_EXEC).get());


        FunctionExecutor executor = null;

        boolean existNextExecutor = false;

        if (eventName != null) {
            FunctionExecutorList functionExecutorList = flow.getFunctionExecutorList(event, eventName);
            if (functionExecutorList != null) {

                FunctionExecutorList.Wrapper wrapper = functionExecutorList.getNextstepFunctionExecutor();

                if (wrapper != null) {

                    executor = wrapper.getFunctionExecutor();

                    if (wrapper.hasNext()) {
                        //plan.emit(event.createChild(eventName));
                        plan.emit(event);
                    }

                }
            }
        }

        if (executor == null) {
            executor = flow.getNextExecutor(event);
            if (executor != null) {
                existNextExecutor = flow.existNextFunctionExecutor(event);
            }
        }


        if (executor != null) {

            Throwable error = null;


            FunctionExecutor.ResultState rs = null;

            try {

                WorkHelper.emitLocalEvent(plan, event, executor.getBeforeFireEventNames(), 0);

                long timeout = executor.getTimeout();
                if (timeout > 0) {
                    WorkHelper.emitLocalEvent(plan, event, executor.getTimeoutFireEventNames(), timeout,
                            new FlowProcessException("timeout"), Thread.currentThread());
                }


                //isFireEvent = executor.execute(event);;
                rs = executor.execute(event);

                WorkHelper.emitLocalEvent(plan, event, executor.getSucessFireEventNames(), 0);

            } catch (Throwable e) {

                error = e;

                WorkHelper.emitLocalEvent(plan, event, executor.getFailFireEventNames(), error);


                String errClassEventName = "error::" + e.getClass().getName();
                WorkHelper.emitLocalEvent(plan, event, errClassEventName, 0, error);


                String errEventName = "error::" + executor.getFireEventUUID();
                WorkHelper.emitLocalEvent(plan, event, errClassEventName, 0, error);


                WorkHelper.emitLocalErrorEvent(plan, event, errEventName, 0, error);


            } finally {
                if (rs != null && rs.isProcessNext()) {

                    WorkHelper.emitLocalEvent(plan, event, rs.getFireEventUUID(), 0);

                    WorkHelper.emitLocalEvent(plan, event, rs.getFireEventName(), executor.getDelayTimeFireEvent());

                    WorkHelper.emitLocalErrorEvent(plan, event, rs.getFireEventName(), executor.getDelayTimeFireEvent(), null);
                }

                WorkHelper.emitLocalEvent(plan, event, executor.getEndFireEventNames(), 0);

            }


            if (executor.isLastExecutor()) {


                return TERMINATE;
            } else {

                if (error != null) {
                    plan.setError(error);
                }

            }

            if (existNextExecutor) {
                //return LOOP;
                return 1;
            }
        }

        return WAIT;
    }


    default ActivatedPlan start() {

        ActivatedPlan p = FlowProcessor.core().execute(this).block();

        return p;

    }

}
