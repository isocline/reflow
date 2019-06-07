package isocline.reflow.examples;

import isocline.reflow.*;
import isocline.reflow.log.XLogger;
import isocline.reflow.module.WorkEventGenerator;


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

    public static void main(String[] args) throws Exception {

        WorkProcessor processor = WorkProcessorFactory.getProcessor();


        EventReceiver checker = new EventReceiver();
        Plan schedule = processor.newPlan(checker).finishTimeFromNow(30 * Clock.SECOND).bindEvent("test").setSleepMode();
        schedule.activate();


        WorkEventGenerator gen = new WorkEventGenerator();
        gen.setEventName("test");

        long startTime = Clock.nextSecond(900);



        processor.newPlan(gen).finishTimeFromNow(30 * Clock.SECOND).setStrictMode().startTime
                (startTime).activate();


        processor.shutdown(20 * Clock.SECOND);
    }
}
