package isocline.reflow.examples.basic;

import isocline.reflow.*;
import isocline.reflow.log.XLogger;
import org.junit.Assert;
import org.junit.Test;


public class DynamicRepeater extends TestBase implements Work {

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

        DynamicRepeater worker = new DynamicRepeater();

        //Re.flow(worker).activate().block();
        FlowProcessor fp = FlowProcessorFactory.getProcessor();
        fp.task(worker).activate().block();

        Assert.assertEquals(worker.seq, 4);

       
    }

}
