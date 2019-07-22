package isocline.reflow.examples.basic;

import isocline.reflow.*;
import isocline.reflow.examples.TestConfiguration;
import isocline.reflow.log.XLogger;
import org.junit.Test;

public class DynamicRepeater implements Work {

    private static XLogger logger = XLogger.getLogger(DynamicRepeater.class);

    private int seq = 0;

    public long execute(WorkEvent event) throws InterruptedException {


        long nexttime = 500 + (long) (Math.random() * 1000);

        logger.debug("activate:" + (seq++) + " nexttime:" + nexttime);

        if (seq > 3) return TERMINATE;

        return nexttime;

    }

    @Test
    public void case1() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan plan = processor.task(new DynamicRepeater());

        plan.activate();

        processor.shutdown(TestConfiguration.TIMEOUT);


    }

}
