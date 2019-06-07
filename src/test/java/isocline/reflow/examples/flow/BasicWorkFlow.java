package isocline.reflow.examples.flow;

import isocline.reflow.FlowableWork;
import isocline.reflow.WorkFlow;
import isocline.reflow.WorkProcessor;
import isocline.reflow.WorkProcessorFactory;
import isocline.reflow.log.XLogger;
import org.junit.Test;


public class BasicWorkFlow implements FlowableWork {


    private static XLogger logger = XLogger.getLogger(BasicWorkFlow.class);

    public void order() {
        logger.debug("invoke - order");

    }


    public void sendSMS() {
        logger.debug("invoke - sendSMS start");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }
        logger.debug("invoke - sendSMS end");


    }

    public void sendMail() {
        logger.debug("invoke - sendMail start");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }
        logger.debug("invoke - sendSMS end");

    }


    public void report() {
        logger.debug("invoke - report");

    }

    public void defineWorkFlow(WorkFlow flow) {

        flow.runAsync(this::order).next(this::sendMail).next(this::sendSMS).next(this::report).finish();


    }


    @Test
    public void test() {
        WorkProcessor processor = WorkProcessorFactory.getProcessor();

        processor.newPlan(this).startDelayTime(2000).activate();

        //processor.activate(this);

        processor.awaitShutdown();


    }


}
