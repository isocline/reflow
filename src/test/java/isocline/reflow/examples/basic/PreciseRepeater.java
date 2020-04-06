package isocline.reflow.examples.basic;

import isocline.reflow.Re;
import isocline.reflow.TestBase;
import isocline.reflow.Work;
import isocline.reflow.WorkEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreciseRepeater extends TestBase implements Work {

    private static Logger logger = LoggerFactory.getLogger(PreciseRepeater.class);

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
