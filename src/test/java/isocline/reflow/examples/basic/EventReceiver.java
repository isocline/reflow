package isocline.reflow.examples.basic;

import isocline.reflow.*;
import isocline.reflow.examples.TestConfiguration;
import isocline.reflow.log.XLogger;
import isocline.reflow.module.WorkEventGenerator;
import org.junit.Test;


/**
 * https://www-01.ibm.com/support/docview.wss?uid=swg21089949
 */
public class EventReceiver implements Work {

    private static XLogger logger = XLogger.getLogger(EventReceiver.class);


    private int failCount = 0;

    public EventReceiver() {

    }


    public long execute(WorkEvent event) throws InterruptedException {


        logger.debug("receive:" + event.getEventName());


        return WAIT;

    }

    @Test
    public void basicStyle() throws Exception {

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


        FlowProcessor.core().shutdown(TestConfiguration.TIMEOUT);
    }


    @Test
    public void simpleStyle() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        processor.task(new EventReceiver(), "example-event").activate();


        WorkEventGenerator gen = new WorkEventGenerator();
        gen.setEventName("example-event");

        processor.task(gen).finishTimeFromNow(30 * Time.SECOND).strictMode().startTime
                (Time.nextSecond()).activate();


        processor.shutdown(TestConfiguration.TIMEOUT);
    }
}
