package isocline.reflow.flow;

import isocline.reflow.*;
import isocline.reflow.log.XLogger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ErrorWorkFlowTest implements FlowableWork {

    private boolean chk = false;

    private boolean chk2 = false;


    private static XLogger logger = XLogger.getLogger(ErrorWorkFlowTest.class);


    public void checkMemory() {
        log("when MEMORY");
    }

    public void checkStorage() {
        log("when STORAGE");
    }

    public void sendSignal() {
        log("send SIGNAL");
        throw new RuntimeException("CUSTOM ERROR");
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

    private void checkError(WorkEvent event) {
        logger.error("when ERROR[1]");

        try {
            String eventNm = event.getEventName();

            Throwable e = event.getThrowable();

            e.printStackTrace();

            logger.error("when ERROR[2]" + e);

            chk2 = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void defineWorkFlow(WorkFlow flow) {

        WorkFlow p1 = flow.runAsync(this::checkMemory).next(this::checkStorage);

        WorkFlow t1 = flow.wait(p1).next(this::sendSignal);

        WorkFlow t2 = flow.wait(p1).next(this::sendStatusMsg).next(this::sendReportMsg);

        flow.onError(t1).next(this::checkError).end();

        flow.waitAll(t1, t2).next(this::report).end();
    }


    @Test
    public void testStartByEvent() {
        FlowProcessor processor = FlowProcessorFactory.getProcessor();

        processor.reflow(this).activate().block();


        assertEquals(false, chk);
        assertEquals(true, chk2);

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
            } catch (Exception ignored) {

            }
        }

        logger.debug(msg + " #END\n");
    }


}
