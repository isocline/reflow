package isocline.reflow.examples.self_control;

import isocline.reflow.*;
import isocline.reflow.examples.TestConfiguration;

import isocline.reflow.log.XLogger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MaxCountTermination implements Work {

    private static XLogger  logger = XLogger.getLogger(MaxCountTermination.class);

    private int count = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        count++;

        logger.debug("activate:" + count);

        if(count==3) {
            return TERMINATE;
        }

        return Clock.SECOND;

    }




    @Test
    public void case1() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        MaxCountTermination checker = new MaxCountTermination();

        Plan schedule = processor.reflow(checker);
        schedule.activate();

        //wait until finish
        schedule.block();

        assertEquals(3, checker.count);


        processor.shutdown(TestConfiguration.TIMEOUT);

    }

}
