package isocline.reflow.examples.basic;

import isocline.reflow.Re;
import isocline.reflow.TestBase;
import isocline.reflow.Work;
import isocline.reflow.WorkEvent;
import isocline.reflow.log.XLogger;
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


        Re.play(new DynamicRepeater()).activate();

       
    }

}
