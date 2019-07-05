package isocline.reflow.examples.microservices.pattern;

import isocline.reflow.Re;
import isocline.reflow.TestUtil;
import isocline.reflow.WorkEvent;
import isocline.reflow.log.XLogger;
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

        Re.flow(flow -> {


            flow.next(this::callService1 , conf->conf.timeout(500)).end();


        }).activate().block();
    }


}
