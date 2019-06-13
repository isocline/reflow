package isocline.reflow.flow;

import isocline.reflow.*;
import isocline.reflow.log.XLogger;
import isocline.reflow.module.WorkEventGenerator;
import org.junit.Test;


public class WorkFlowTest implements FlowableWork {


    private static XLogger logger = XLogger.getLogger(WorkFlowTest.class);


    public void order() {
        logger.debug("invoke - order");

    }


    public void sendSMS() {
        logger.debug("invoke - sendSMS start ############################");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }
        logger.debug("invoke - sendSMS end");


    }

    public void sendMail() {
        logger.debug("invoke - sendMail start");
        try {
            Thread.sleep(2500);
        } catch (Exception e) {

        }
        logger.debug("invoke - sendMail end");

    }


    public void report() {
        logger.debug("");
        try {
            Thread.sleep(2000);
        } catch (Exception e) {

        }
        logger.debug("************ invoke - report************");

    }


    public void timeout(){
        logger.debug("");
        logger.debug("********** TIME OUT *************");

    }

    public void defineWorkFlow2(WorkFlow flow) {

        flow.next(this::order).runAsync(this::sendMail,"mail").runAsync(this::sendSMS,"x1");

        flow.wait("mail").next(this::report,"x2");

        flow.waitAll("x1","x2","qq").finish();


        flow.wait("error").next(this::report);


    }

    public void defineWorkFlow(WorkFlow flow) {

        flow.runAsync(this::order).next(this::sendMail,"mail").next(this::sendSMS,"x1");

        flow.wait("mail").next(this::report,"x2");

        flow.waitAll("x1","x2").finish();


    }

    public void defineWorkFlow_XX(WorkFlow flow) {

        flow.runAsync(this::order).next(this::sendMail,"mail").next(this::sendSMS,"x1");

        flow.wait("mail").next(this::report).fireEvent("qq",2000);



        flow.wait("qq").finish();


    }

    public void defineWorkFlow3(WorkFlow flow) {

        flow.fireEvent("timeout",3000).runAsync(this::order).next(this::sendMail,"mail").next(this::sendSMS,"x1");

        flow.wait("mail").next(this::sendMail).fireEvent("qq",2000);

        flow.wait("timeout").next(this::timeout).finish();

        flow.waitAll("qq").next(this::report).finish();


    }

    @Test
    public void testSimple() {
        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        //processor.Reflow(this).startDelayTime(1000).activate();
        processor.reflow(this).activate();


        processor.awaitShutdown();


    }

    @Test
    public void testStartByEvent() {
        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        processor.reflow(this::defineWorkFlow3).on("start").activate();


        WorkEventGenerator gen = new WorkEventGenerator();
        gen.setEventName("start");
        gen.setRepeatTime(Work.TERMINATE);

        processor.reflow(gen).startDelayTime(1000).activate();

        processor.awaitShutdown();


    }


}
