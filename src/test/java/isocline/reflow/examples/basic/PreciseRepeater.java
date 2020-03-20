package isocline.reflow.examples.basic;

import isocline.reflow.Re;
import isocline.reflow.TestBase;
import isocline.reflow.Work;
import isocline.reflow.WorkEvent;
import isocline.reflow.log.XLogger;
import org.junit.Test;

public class PreciseRepeater extends TestBase implements Work {

    private static XLogger logger = XLogger.getLogger(PreciseRepeater.class);

    private int seq = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        logger.debug("activate:" + seq++);


        // repeat 100 times
        if (seq > 100) return TERMINATE;

        // execute after 10 milli seconds
        return 10;
    }


    /**
     *
     *
     *
     */
    @Test
    public void case1()   {

        Re.flow(this)
                .strictMode()
                .activate();

        // check logging time


    }

}
