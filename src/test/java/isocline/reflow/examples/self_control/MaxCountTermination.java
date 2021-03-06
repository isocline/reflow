package isocline.reflow.examples.self_control;

import isocline.reflow.*;
import isocline.reflow.examples.TestConfiguration;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class MaxCountTermination implements Work {

    private static Logger logger = LoggerFactory.getLogger(MaxCountTermination.class);

    private int count = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        count++;

        logger.debug("activate:" + count);

        if(count==3) {
            return TERMINATE;
        }

        return Time.SECOND;

    }




    @Test
    public void case1() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        MaxCountTermination checker = new MaxCountTermination();

        Plan schedule = processor.task(checker);
        schedule.activate().block();

        //wait until inactive


        assertEquals(3, checker.count);


        processor.shutdown(TestConfiguration.TIMEOUT);

    }

}
