package isocline.reflow.examples.basic;

import isocline.reflow.Re;
import isocline.reflow.TestBase;
import isocline.reflow.Work;
import isocline.reflow.WorkEvent;
import isocline.reflow.log.XLogger;
import org.junit.Test;

public class SimpleExecute extends TestBase implements Work {

    private static XLogger logger = XLogger.getLogger(SimpleExecute.class);

    private int seq = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        logger.debug("activate:" + seq++);


        return TERMINATE;

    }

    @Test
    public void case1() throws Exception {

        Re.play(new SimpleExecute()).activate();


    }


}
