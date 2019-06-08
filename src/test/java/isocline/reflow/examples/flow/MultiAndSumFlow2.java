package isocline.reflow.examples.flow;

import isocline.reflow.*;
import isocline.reflow.log.XLogger;
import org.junit.Test;


public class MultiAndSumFlow2 implements FlowableWork {


    private static XLogger logger = XLogger.getLogger(MultiAndSumFlow2.class);

    public void async1() {
        logger.debug("** invoke - async1");
        TestUtil.waiting(500);
        logger.debug("** invoke - async1 - END");
    }

    public void async2() {
        logger.debug("** invoke - async2");
        TestUtil.waiting(600);
        logger.debug("** invoke - async2 - END");
    }


    public void sum1() {
        logger.debug("** invoke - sum1");
        TestUtil.waiting(300);
        logger.debug("** invoke - sum1 - END");
    }

    public void async3() {
        logger.debug("** invoke - async3");
        TestUtil.waiting(500);
        logger.debug("** invoke - async1 - END");
    }

    public void async4() {
        logger.debug("** invoke - async4");
        TestUtil.waiting(600);
        logger.debug("** invoke - async2 - END");
    }


    public void sum2() {
        logger.debug("** invoke - sum2");
        TestUtil.waiting(300);
        logger.debug("** invoke - sum2 - END");
    }



    public void defineWorkFlow(WorkFlow flow) {

        flow.runAsync(this::async1).runAsync(this::async2);

        flow.waitAll().next(this::sum1);

        flow.runAsync(this::async3).runAsync(this::async4);

        flow.waitAll().next(this::sum2).finish();

    }


    @Test
    public void test() throws InterruptedException {
        Plan schedule = start();

        schedule.block();

        FlowProcessorFactory.getProcessor().awaitShutdown();


    }


}
