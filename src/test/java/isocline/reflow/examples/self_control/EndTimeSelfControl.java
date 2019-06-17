package isocline.reflow.examples.self_control;

import isocline.reflow.*;
import isocline.reflow.examples.TestConfiguration;
import isocline.reflow.log.XLogger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EndTimeSelfControl implements Work {

    private static XLogger logger = XLogger.getLogger(EndTimeSelfControl.class);

    private int count = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        count++;

        logger.debug("activate:" + count);

        if(count ==1) {
            event.getActivity().finishFromNow(Time.SECOND*2);
        }

        return Time.SECOND/2;

    }


    @Test
    public void case1() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();

        EndTimeSelfControl checker = new EndTimeSelfControl();

        Plan schedule = processor.reflow(checker);
        schedule.activate().block();



        assertEquals(4, checker.count);

        processor.shutdown(TestConfiguration.TIMEOUT);

    }

}
