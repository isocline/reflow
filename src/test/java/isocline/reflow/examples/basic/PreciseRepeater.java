package isocline.reflow.examples.basic;

import isocline.reflow.*;
import isocline.reflow.examples.TestConfiguration;
import isocline.reflow.log.XLogger;
import org.junit.Test;

public class PreciseRepeater implements Work {

    private static XLogger logger = XLogger.getLogger(PreciseRepeater.class);

    private int seq = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        logger.debug("activate:" + seq++);

        return 10;
    }

    @Test
    public void case1() throws Exception {

        Re.call(new PreciseRepeater())
                .strictMode()
                .activate();


        FlowProcessor.core().shutdown(TestConfiguration.TIMEOUT);
    }

}
