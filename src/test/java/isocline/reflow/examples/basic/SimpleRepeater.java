package isocline.reflow.examples.basic;

import isocline.reflow.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleRepeater extends TestBase implements Work {

    private static Logger logger = LoggerFactory.getLogger(SimpleRepeater.class);

    private int seq = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        logger.debug("activate:" + seq++);


        return WAIT;
        //return Time.SECOND;

    }

    @Test
    public void case1() throws Exception {

        Re.flow(new SimpleRepeater())
                .interval(1 * Time.SECOND)
                .finishTimeFromNow(5 * Time.SECOND)
                .activate();

    }


    @Test
    public void startMethod() throws Exception {

        Re.flow((WorkEvent event) -> {
            // DO YOUR WORK
            return 10 * Time.SECOND;
        })
                .finishTimeFromNow(5 * Time.SECOND)
                .activate();


    }


    @Test
    public void case2() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.task(SimpleRepeater.class);

        schedule.interval(1 * Time.SECOND);
        schedule.activate();


    }

    @Test
    public void caseStrictMode() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.task(SimpleRepeater.class);

        schedule.interval(1 * Time.SECOND);
        schedule.strictMode();
        schedule.activate();


    }

    @Test
    public void delayStart1() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.task(SimpleRepeater.class);

        schedule.interval(1 * Time.SECOND);
        schedule.initialDelay(Time.milliseconds(0, 0, 2));
        schedule.activate();

    }


    @Test
    public void delayStart2() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.task(SimpleRepeater.class);

        schedule.interval(1 * Time.SECOND);
        schedule.startTime(Time.nextSecond() + Time.SECOND * 2);
        schedule.activate();


    }
}
