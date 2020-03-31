package isocline.reflow.examples.flow;

import isocline.reflow.*;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ComplexWorkFlow implements FlowableWork {


    private static Logger logger = LoggerFactory.getLogger(ComplexWorkFlow.class);

    public void order() {
        logger.debug("** invoke - order");

    }


    public void sendSMS() {
        logger.debug("invoke - sendSMS ** start");
        try {
            Thread.sleep(1000);
        } catch (Exception ignored) {

        }
        logger.debug("invoke - sendSMS ** end");


    }

    public void sendMail() {
        logger.debug("invoke - sendMail start");
        try {
            Thread.sleep(2000);
        } catch (Exception ignored) {

        }
        logger.debug("invoke - sendMail end");

    }


    public void report() {
        logger.debug("invoke - report");

    }

    public void report2(WorkEvent event) {
        logger.debug("invoke - report2 " + event.getEventName() + " "+event);

    }

    public void defineWorkFlow(WorkFlow flow) {

        flow.runAsync(this::order).next(this::sendMail, "h1");
        flow.runAsync(this::sendSMS).next(this::report, "h2");

        flow.waitAll("h1","h2").next(this::report2).end();

    }


    @Test
    public void test() throws InterruptedException {

        Activity activity = start();
        logger.debug("start complete");

    }

    @Test
    public void test2() throws Exception {
        FlowProcessor processor = FlowProcessorFactory.getProcessor();

        processor.reflow(this).activate().block();

        logger.info("END");
    }


    @After
    public void compeleteTest() {
        //FlowProcessorFactory.getProcessor().awaitShutdown();
    }

}
