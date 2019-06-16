package isocline.reflow.examples.basic;

import isocline.reflow.*;
import isocline.reflow.examples.TestConfiguration;
import isocline.reflow.log.XLogger;
import org.junit.Test;

public class SimpleRepeater implements Work {

    private static XLogger logger = XLogger.getLogger(SimpleRepeater.class);

    private int seq = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        logger.debug("activate:" + seq++);


        return WAIT;
        //return Time.SECOND;

    }

    @Test
    public void case1() throws Exception {


        Plan Plan = FlowProcessor.core()
                .reflow(new SimpleRepeater())
                .interval(1 * Time.SECOND)
                .finishTimeFromNow(5 * Time.SECOND);

        Plan.activate().block();

        FlowProcessor.core().shutdown(TestConfiguration.TIMEOUT);
        //processor.awaitShutdown();


    }


    @Test
    public void case2() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.reflow(SimpleRepeater.class);

        schedule.interval(1 * Time.SECOND);
        schedule.activate();


        processor.shutdown(TestConfiguration.TIMEOUT);
    }

    @Test
    public void caseStrictMode() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.reflow(SimpleRepeater.class);

        schedule.interval(1 * Time.SECOND);
        schedule.strictMode();
        schedule.activate();


        processor.shutdown(TestConfiguration.TIMEOUT);
    }

    @Test
    public void delayStart1() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.reflow(SimpleRepeater.class);

        schedule.interval(1 * Time.SECOND);
        schedule.startDelayTime(Time.milliseconds(0, 0, 2));
        schedule.activate();


        processor.shutdown(TestConfiguration.TIMEOUT);
    }


    @Test
    public void delayStart2() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.reflow(SimpleRepeater.class);

        schedule.interval(1 * Time.SECOND);
        schedule.startTime(Time.nextSecond() + Time.SECOND * 2);
        schedule.activate();


        processor.shutdown(TestConfiguration.TIMEOUT);
    }
}
