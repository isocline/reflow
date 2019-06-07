package isocline.reflow.flow;

import isocline.reflow.FlowableWork;
import isocline.reflow.WorkFlow;
import isocline.reflow.WorkProcessor;
import isocline.reflow.WorkProcessorFactory;
import isocline.reflow.log.XLogger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class BasicWorkFlowTest implements FlowableWork {

    private boolean chk = false;


    private static XLogger logger = XLogger.getLogger(BasicWorkFlowTest.class);


    public void checkMemory() {
        log("check MEMORY");
    }

    public void checkStorage() {
        log("check STORAGE");
    }

    public void sendSignal() {
        log("send SIGNAL");
    }

    public void sendStatusMsg() {
        log("send STATUS MSG");
    }

    public void sendReportMsg() {
        log("send REPORT MSG");
    }

    public void report() {
        log("REPORT");
        chk = true;

    }


    public void defineWorkFlow(WorkFlow flow) {

        WorkFlow p1 = flow.next(this::checkMemory).next(this::checkStorage);

        WorkFlow t1 = flow.wait(p1).next(this::sendSignal);

        WorkFlow t2 = flow.wait(p1).next(this::sendStatusMsg).next(this::sendReportMsg);

        flow.waitAll(t1, t2).next(this::report).finish();
    }


    @Test
    public void testStartByEvent() {
        WorkProcessor processor = WorkProcessorFactory.getProcessor();

        processor.newPlan(this).activate();

        processor.awaitShutdown();

        assertEquals(true, chk);

    }

    private void log(String msg) {
        log(msg, -1);
    }

    private void log(String msg, long delayTime) {
        logger.debug(msg + " #START");

        if (delayTime == -1) {
            delayTime = 500 + (long) (3000 * Math.random());
        }

        if (delayTime > 0) {

            try {
                logger.debug(msg + " #WAIT - " + delayTime);
                Thread.sleep(delayTime);
            } catch (Exception e) {

            }
        }

        logger.debug(msg + " #END\n");
    }


}
