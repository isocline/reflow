package isocline.reflow.examples;

import isocline.reflow.*;
import isocline.reflow.module.WorkEventGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * https://www-01.ibm.com/support/docview.wss?uid=swg21089949
 */
public class EventReceiver implements Work {

    private static Logger logger = LoggerFactory.getLogger(EventReceiver.class);


    private int failCount = 0;

    public EventReceiver() {

    }


    public long execute(WorkEvent event) throws InterruptedException {

        logger.debug("receive:" + event.getEventName());


        return WAIT;

    }

    public static void main(String[] args) throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        EventReceiver checker = new EventReceiver();
        Plan schedule = processor.task(checker).finishTimeFromNow(30 * Time.SECOND).on("test").daemonMode();
        schedule.activate();


        WorkEventGenerator gen = new WorkEventGenerator();
        gen.setEventName("test");

        long startTime = Time.nextSecond(900);



        processor.task(gen).finishTimeFromNow(30 * Time.SECOND).strictMode().startTime
                (startTime).activate();


        processor.shutdown(20 * Time.SECOND);
    }
}
