package isocline.reflow.examples.flow;

import isocline.reflow.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MultiAndSumFlow implements FlowableWork {


    private static Logger logger = LoggerFactory.getLogger(MultiAndSumFlow.class);

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

        flow.runAsync(this::async1,"h1").runAsync(this::async2,"h2");

        flow.waitAll("h1","h2").next(this::sum1);

        flow.runAsync(this::async3,"h3").runAsync(this::async4,"h4");

        flow.waitAll("h3","h4").next(this::sum2).end();

    }


    @Test
    public void test() throws InterruptedException {

        Activity plan  = start(false);

        System.err.println("--1");
        plan.block();
        System.err.println("--2");
        Thread.sleep(10000);

        FlowProcessorFactory.getProcessor().awaitShutdown();


    }


}
