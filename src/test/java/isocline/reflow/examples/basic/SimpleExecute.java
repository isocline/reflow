package isocline.reflow.examples.basic;

import isocline.reflow.FlowProcessor;
import isocline.reflow.FlowProcessorFactory;
import isocline.reflow.Work;
import isocline.reflow.WorkEvent;
import isocline.reflow.examples.TestConfiguration;
import isocline.reflow.log.XLogger;
import org.junit.Test;

public class SimpleExecute implements Work {

    private static XLogger logger = XLogger.getLogger(SimpleExecute.class);

    private int seq = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        logger.debug("activate:" + seq++);


        return TERMINATE;

    }

    @Test
    public void case1() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();



        // activate async
        processor.execute(new SimpleExecute());


        processor.shutdown(TestConfiguration.TIMEOUT);

    }


}
