package isocline.reflow.examples.flow;

import isocline.reflow.FlowProcessor;
import isocline.reflow.FlowProcessorFactory;
import isocline.reflow.FlowableWork;
import isocline.reflow.WorkFlow;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BasicWorkFlow implements FlowableWork {


    private static Logger logger = LoggerFactory.getLogger(BasicWorkFlow.class);

    private int invokeCount = 0;

    public void order() {
        logger.debug("invoke - order");
        invokeCount++;

    }


    public void sendSMS() {
        logger.debug("invoke - sendSMS start");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }
        logger.debug("invoke - sendSMS end");
        invokeCount++;


    }

    public void sendMail() {
        logger.debug("invoke - sendMail start");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }
        logger.debug("invoke - sendMail end");
        invokeCount++;

    }


    public void report() {
        logger.debug("invoke - report");
        invokeCount++;

    }

    public void defineWorkFlow(WorkFlow flow) {

        flow.runAsync(this::order).run(this::sendMail).run(this::sendSMS).run(this::report).end();


    }


    @Test
    public void test() {
        FlowProcessor processor = FlowProcessorFactory.getProcessor();

        processor.reflow(this).activate().block();

        logger.info("END");

        Assert.assertEquals(4, invokeCount);


        processor.awaitShutdown();


    }


}
