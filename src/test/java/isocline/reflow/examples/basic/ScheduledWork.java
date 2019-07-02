package isocline.reflow.examples.basic;

import isocline.reflow.*;
import isocline.reflow.descriptor.CronDescriptor;
import isocline.reflow.examples.TestConfiguration;
import isocline.reflow.log.XLogger;
import org.junit.Test;

public class ScheduledWork implements Work {

    private static XLogger logger = XLogger.getLogger(ScheduledWork.class);

    private int seq = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        logger.debug("activate:" + seq++);


        return WAIT;
    }

    @Test
    public void caseSchedule() throws Exception {

        Re.play(new ScheduledWork())
                .interval(1 * Time.HOUR)
                .startTime("2020-04-24T09:00:00Z")
                .finishTime("2020-06-16T16:00:00Z")
                .activate();


        FlowProcessor.core().shutdown(TestConfiguration.TIMEOUT);
        //processor.awaitShutdown();
    }


    @Test
    public void caseCron() throws Exception {

        Re.play(new CronDescriptor("* 1,4-6 * * *"), new ScheduledWork())
                .finishTime("2020-06-16T16:00:00Z")
                .activate();


        FlowProcessor.core().shutdown(TestConfiguration.TIMEOUT);
        //processor.awaitShutdown();
    }


    @Test
    public void case2() throws Exception {

        FlowProcessor manager = FlowProcessorFactory.getProcessor();


        Plan schedule = manager.task(ScheduledWork.class);

        schedule.interval(1 * Time.SECOND);
        schedule.activate();


        manager.shutdown(TestConfiguration.TIMEOUT);
    }

    @Test
    public void caseStrictMode() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.task(ScheduledWork.class);

        schedule.interval(1 * Time.SECOND);
        schedule.strictMode();
        schedule.activate();


        processor.shutdown(TestConfiguration.TIMEOUT);
    }

    @Test
    public void delayStart1() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.task(ScheduledWork.class);

        schedule.interval(1 * Time.SECOND);
        schedule.initialDelay(Time.milliseconds(0, 0, 2));
        schedule.activate();


        processor.shutdown(TestConfiguration.TIMEOUT);
    }


    @Test
    public void delayStart2() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.task(ScheduledWork.class);

        schedule.interval(1 * Time.SECOND);
        schedule.startTime(Time.nextSecond() + Time.SECOND * 2);
        schedule.activate();


        processor.shutdown(TestConfiguration.TIMEOUT);
    }
}
