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
     * 
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
    void defineWorkFlow(WorkFlow<T> flow) ;


    /**
     * It is not necessary to implement additional methods as extended methods implemented from the Work interface.
     * DO NOT implement this method.
     *
     * @return delay time
     * @throws InterruptedException If interrupt occur
     */
    default long execute(WorkEvent event) throws InterruptedException {


        final ActivatedPlan schedule = event.getPlan();

        final WorkFlow flow = schedule.getWorkFlow();

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
                        schedule.emit(event.createChild(eventName));
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

            boolean isFireEvent = false;

            final String fireEventName = executor.getFireEventName();

            FunctionExecutor.ResultState rs = null;

            try {
                if (event.getThrowable() != null) {
                    isFireEvent = true;
                }


                //isFireEvent = executor.execute(event);;
                rs = executor.execute(event);

            } catch (Throwable e) {

                error = e;

                String errClassEventName = "error::"+e.getClass().getName() ;

                //WorkEvent errClsEvent = WorkEventFactory.createOrigin(errClassEventName);
                //WorkEvent errClsEvent = WorkEventFactory.createWithOrigin(errClassEventName,event.origin());
                WorkEvent errClsEvent = event.createChild(errClassEventName);
                        errClsEvent.setThrowable(e);

                schedule.emit(errClsEvent);


                if (fireEventName != null) {
                    String errEventName = "error::"+ fireEventName;

                    //WorkEvent errEvent = WorkEventFactory.createOrigin(errEventName);
                    WorkEvent errEvent = event.createChild(errEventName);
                    errEvent.setThrowable(e);

                    schedule.emit(errEvent);

                }

                String errEventName = "error::"+ executor.getFireEventUUID() ;

                //final WorkEvent errEvent = WorkEventFactory.createOrigin(errEventName);
                final WorkEvent errEvent = event.createChild(errEventName);
                errEvent.setThrowable(e);


                schedule.emit(errEvent);


                //final WorkEvent errEvent2 = WorkEventFactory.createOrigin(WorkFlow.ERROR);
                final WorkEvent errEvent2 = event.createChild(errEventName);
                errEvent2.setFireEventName(WorkFlow.ERROR);
                errEvent2.setThrowable(e);

                schedule.emit(errEvent2);
            } finally {
                if (rs!=null && rs.isProcessNext()) {

                    WorkHelper.emitLocalEvent(schedule,event, rs.getFireEventUUID(),0);

                    WorkHelper.emitLocalEvent(schedule,event, rs.getFireEventName(),executor.getDelayTimeFireEvent());

                }
            }


            if (executor.isLastExecutor()) {
                return TERMINATE;
            }else {

                if(error!=null) {
                    schedule.setError(error);
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
