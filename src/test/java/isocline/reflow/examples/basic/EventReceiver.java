package isocline.reflow.examples.basic;

import isocline.reflow.*;
import isocline.reflow.log.XLogger;
import isocline.reflow.module.WorkEventGenerator;
import org.junit.Test;


/**
 * https://www-01.ibm.com/support/docview.wss?uid=swg21089949
 */
public class EventReceiver extends TestBase implements Work {

    private static XLogger logger = XLogger.getLogger(EventReceiver.class);


    private int failCount = 0;

    public EventReceiver() {

    }


    public long execute(WorkEvent event) throws InterruptedException {


        logger.debug("receive:" + event.getEventName());


        return WAIT;

    }

    @Test
    public void invokeRepeat() throws Exception {

        // Receiver
        Re.play(new EventReceiver()).on("example-event")
                .activate();

        // Emitter
        WorkEventGenerator gen = new WorkEventGenerator();
        gen.setEventName("example-event");

        Re.play(gen)
                .strictMode()
                .interval(1 * Time.SECOND)
                .startTime(Time.nextSecond())
                .finishTimeFromNow(30 * Time.SECOND)
                .activate();



    }


    @Test
    public void simpleStyle() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        processor.task(new EventReceiver(), "example-event").activate();


        WorkEventGenerator gen = new WorkEventGenerator();
        gen.setEventName("example-event");

        processor.task(gen).strictMode()
                .startTime(Time.nextSecond()) //start next exact second
                .finishTimeFromNow(30 * Time.SECOND) // finish after 30 secs but this event process fire event only one.
                .activate();



    }
}
