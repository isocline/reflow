package isocline.reflow.examples.microservices.pattern;

import isocline.reflow.FlowProcessor;
import isocline.reflow.TestUtil;
import isocline.reflow.WorkEvent;
import isocline.reflow.log.XLogger;
import isocline.reflow.pattern.Timeout;
import org.junit.Test;

public class TimeoutPattern {

    private static int CNT = 0;

    private XLogger logger = XLogger.getLogger(TimeoutPattern.class);

    public void init() {
        logger.debug("init");
    }

    public void callService1(WorkEvent e) {
        CNT++;

        logger.debug("Service1 - start " + CNT);
        TestUtil.waiting(3000);
        logger.debug("Service1 - end " + CNT);


    }

    public static int count = 0;


    @Test
    public void startTest() {

        FlowProcessor.core()

                .reflow(flow -> {

                    flow.pattern(
                            Timeout.setup(6000), () -> {
                                flow.next(this::callService1).end();
                            }
                    );

                }).activate().block();
    }


}
