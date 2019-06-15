package isocline.reflow.flow;

import isocline.reflow.FlowProcessorFactory;
import isocline.reflow.FlowableWork;
import isocline.reflow.WorkFlow;
import isocline.reflow.FlowProcessor;
import isocline.reflow.log.XLogger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TestWorkFlowTest implements FlowableWork {

    private boolean chk = false;


    private static XLogger logger = XLogger.getLogger(TestWorkFlowTest.class);


    public void checkMemory() {
        log("when MEMORY");
    }

    public void checkStorage() {
        log("when STORAGE");
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

        flow.runAsync(this::sendSignal);

        WorkFlow t2 = flow.wait(p1).next(this::sendStatusMsg).next(this::sendReportMsg);

        flow.waitAll( t2).next(this::report).end();
    }


    @Test
    public void testStartByEvent() {
        FlowProcessor processor = FlowProcessorFactory.getProcessor();

        processor.reflow(this).activate();

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
