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

        if(event.origin().isComplete()) {
            return TERMINATE;
        }


        final Activity activity = event.getActivity();

        final WorkFlow flow = activity.getWorkFlow();

        if (flow == null) {
            return TERMINATE;
        }

        final String eventName = event.getFireEventName();



        FunctionExecutor executor = null;

        boolean existNextExecutor = false;


        if (eventName != null) {
            FunctionExecutorList functionExecutorList = flow.getFunctionExecutorList(event, eventName);

            if (functionExecutorList != null) {
                FunctionExecutorList.Wrapper wrapper = functionExecutorList.getNextstepFunctionExecutor();

                if (wrapper != null) {

                    executor = wrapper.getFunctionExecutor();

                    if (wrapper.hasNext()) {
                        // In case of user-defined event, additional event is generated to call the event repeatedly
                        activity.emit(event);
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

                WorkHelper.emitLocalEvent(activity, event, executor.getBeforeFireEventNames(), 0);

                long timeout = executor.getTimeout();
                if (timeout > 0) {
                    WorkHelper.emitLocalEvent(activity, event, executor.getTimeoutFireEventNames(), timeout,
                            new FlowProcessException("timeout"), Thread.currentThread());
                }

                //System.out.println(" < ==< "+event.getEventName() + " "+event.getFireEventName());

                executor.check();


                ((PlanImpl) activity).checkEvent(event);


                //isFireEvent = executor.execute(event);;
                rs = executor.execute(event);

                executor.onSuccess();

                WorkHelper.emitLocalEvent(activity, event, executor.getSucessFireEventNames(), 0);

            } catch (Throwable err) {

                error = err;

                executor.onFail();

                WorkHelper.emitLocalEvent(activity, event, executor.getFailFireEventNames(), err);


                String errClassEventName = WorkEventKey.PREFIX_ERROR + err.getClass().getName();
                WorkHelper.emitLocalEvent(activity, event, errClassEventName, 0, err);


                String errEventName = WorkEventKey.PREFIX_ERROR + executor.getFireEventUUID();
                WorkHelper.emitLocalEvent(activity, event, errClassEventName, 0, err);


                WorkHelper.emitLocalErrorEvent(activity, event, errEventName, 0, err);


            } finally {
                if (rs != null && rs.isProcessNext()) {

                    WorkHelper.emitLocalEvent(activity, event, rs.getFireEventUUID(), 0);

                    WorkHelper.emitLocalEvent(activity, event, rs.getFireEventName(), executor.getDelayTimeFireEvent());

                    WorkHelper.emitLocalErrorEvent(activity, event, rs.getFireEventName(), executor.getDelayTimeFireEvent(), null);
                }

                WorkHelper.emitLocalEvent(activity, event, executor.getEndFireEventNames(), 0);

            }


            if (executor.isLastExecutor()) {


                //event.origin().complete();

                return TERMINATE;
            } else {

                if (error != null) {
                    activity.setError(error);
                }

            }

            if (existNextExecutor) {
                return LOOP;
                //return 1;
            }
        }

        return WAIT;
    }


    /**
     * Executes this object according to the implementation flow logic.
     *
     * @param isAsync true to execute asynchronously ; false to execute synchronously.
     * @return an instance of Activity
     */
    default Activity start(boolean isAsync) {

        Activity activity = FlowProcessor.core().execute(this);
        if(!isAsync) {
            activity.block();
        }

        return  activity;

    }


    default Activity start() {
        return start(true);
    }

}
