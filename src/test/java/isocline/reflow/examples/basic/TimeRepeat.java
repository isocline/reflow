package isocline.reflow.examples.basic;

import isocline.reflow.*;
import isocline.reflow.descriptor.CronDescriptor;
import isocline.reflow.log.XLogger;
import org.junit.Test;

public class TimeRepeat extends TestBase implements Work {

    private static XLogger logger = XLogger.getLogger(TimeRepeat.class);

    public long execute(WorkEvent event) throws InterruptedException {
        logger.debug("13");

        return 24 * Time.HOUR;

    }

    @Test
    public void case1() throws Exception {

        Re.play(new CronDescriptor("49 1 * * *"), this).activate();

    }


}
