package isocline.reflow.flow;

import isocline.reflow.*;
import isocline.reflow.module.WorkEventGenerator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WorkFlowTest implements FlowableWork {


    private static Logger logger = LoggerFactory.getLogger(WorkFlowTest.class);


    public void order() {
        logger.debug("invoke - order");

    }


    public void sendSMS() {
        logger.debug("invoke - sendSMS start ############################");
        try {
            Thread.sleep(1000);
        } catch (Exception ignored) {

        }
        logger.debug("invoke - sendSMS end");


    }

    public void sendMail() {
        logger.debug("invoke - sendMail start");
        try {
            Thread.sleep(2500);
        } catch (Exception ignored) {

        }
        logger.debug("invoke - sendMail end");

    }


    public void report() {


        logger.debug("************ invoke - report************");

    }


    public void timeout(){
        logger.debug("");
        logger.debug("********** TIME OUT *************");

    }

    public void defineWorkFlow2(WorkFlow flow) {

        flow.run(this::order).runAsync(this::sendMail,"mail").runAsync(this::sendSMS,"x1");

        flow.wait("mail").run(this::report,"x2");

        flow.waitAll("x1","x2","qq").end();


        flow.wait("error").run(this::report);


    }

    public void defineWorkFlow(WorkFlow flow) {

        flow.runAsync(this::order).run(this::sendMail,"mail").run(this::sendSMS,"x1");

        flow.wait("mail").run(this::report,"x2");

        flow.waitAll("x1","x2").run(this::report).end();


    }


    public void defineWorkFlow_XX(WorkFlow flow) {

        flow.runAsync(this::order).run(this::sendMail,"mail").run(this::sendSMS,"x1");

        flow.wait("mail").run(this::report).fireEvent("qq",2000);



        flow.wait("qq").end();


    }

    public void defineWorkFlow3(WorkFlow flow) {

        flow.fireEvent("timeout",3000).runAsync(this::order).run(this::sendMail,"mail").run(this::sendSMS,"x1");

        flow.wait("mail").run(this::sendMail).fireEvent("qq",2000);

        flow.wait("timeout").run(this::timeout).end();

        flow.waitAll("qq").run(this::report).end();


    }

    @Test
    public void testSimple() {
        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        //processor.Reflow(this).initialDelay(1000).activate();
        processor.reflow(this).activate();


        processor.awaitShutdown();


    }

    @Test
    public void testStartByEvent() {
        FlowProcessor processor = FlowProcessor.core();


        processor.reflow(this::defineWorkFlow3).on("start").activate();


        WorkEventGenerator gen = new WorkEventGenerator();
        gen.setEventName("start");
        gen.setIntervalTime(Work.TERMINATE);

        processor.task(gen).initialDelay(1000).activate();

        FlowProcessor.core().shutdown(5000);


    }


}
