package isocline.reflow.examples.basic;

import isocline.reflow.*;
import isocline.reflow.examples.TestConfiguration;
import isocline.reflow.log.XLogger;
import org.junit.Test;

public class DynamicRepeater implements Work {

    private static XLogger logger = XLogger.getLogger(DynamicRepeater.class);

    private int seq = 0;

    public long execute(WorkEvent event) throws InterruptedException {


        long nexttime = 500 + (long) (Math.random()*1000);

        logger.debug("activate:" + (seq++) + " nexttime:"+nexttime );


        return nexttime;


    }

    @Test
    public void case1() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();



        Planning schedule = processor.reflow(new DynamicRepeater());

        schedule.activate();

        processor.shutdown(TestConfiguration.TIMEOUT);



    }

}
