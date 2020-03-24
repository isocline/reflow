package isocline.reflow.examples.basic;

import isocline.reflow.*;
import isocline.reflow.module.WorkEventGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * https://www-01.ibm.com/support/docview.wss?uid=swg21089949
 */
public class EventReceiver extends TestBase implements Work {

    private static Logger logger = LoggerFactory.getLogger(EventReceiver.class);


    private int receiveCount = 0;

    public EventReceiver() {
        shutdownTimeout = 5000;
    }


    public long execute(WorkEvent event) throws InterruptedException {
        receiveCount++;

        logger.debug("receive:" + event.getEventName());
        return WAIT;

    }



    @Test
    public void testBasicStyle() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();

        processor.task(this, "example-event").activate();


        WorkEventGenerator gen = new WorkEventGenerator();
        gen.setEventName("example-event");

        processor.task(gen).strictMode()
                .interval(1 * Time.SECOND)
                .startTime(Time.nextSecond()) //start next exact second
                .finishTimeFromNow(5 * Time.SECOND) // finish after 30 secs but this event process fire event only one.
                .activate().block();


        Assert.assertEquals(5, receiveCount);

    }


    @Test
    public void testSimpleStyle() throws Exception {


        // Receiver
        Re.flow(this)
                .daemonMode()
                .on("example-event")
                .activate();


        // Emitter
        WorkEventGenerator gen = new WorkEventGenerator();
        gen.setEventName("example-event");


        Re.flow(gen)
                .strictMode()
                .interval(1 * Time.SECOND)
                .startTime(Time.nextSecond())
                .finishTimeFromNow(5 * Time.SECOND)
                .activate()
                .block();


        Assert.assertEquals(5, receiveCount);
    }

}
