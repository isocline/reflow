package isocline.reflow.flow;

import isocline.reflow.FlowableWork;
import isocline.reflow.Re;
import isocline.reflow.WorkFlow;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;


public class BasicWorkFlowTest implements FlowableWork {

    private boolean chk = false;


    private static Logger logger = LoggerFactory.getLogger(BasicWorkFlowTest.class);


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

        WorkFlow p1 = flow.run(this::checkMemory).run(this::checkStorage);

        WorkFlow t1 = flow.wait(p1).run(this::sendSignal);

        WorkFlow t2 = flow.wait(p1).run(this::sendStatusMsg).run(this::sendReportMsg);

        flow.waitAll(t1, t2).run(this::report).end();
    }


    @Test
    public void testStartByEvent() {

        Re.flow(this).activate().block();


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
            } catch (Exception ignored) {

            }
        }

        logger.debug(msg + " #END\n");
    }


}
