package isocline.reflow.examples.microservices.pattern;

import isocline.reflow.Re;
import isocline.reflow.TestUtil;
import isocline.reflow.WorkEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutPattern {

    private static int CNT = 0;

    private Logger logger = LoggerFactory.getLogger(TimeoutPattern.class);

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


            flow.accept(this::callService1 , conf->conf.timeout(500)).end();


        }).activate().block();
    }


    @Test
    public void startTest2() {

        Re.flow(flow -> {


            flow.fireEvent("tt",500).accept(this::callService1).end();

            flow.wait("tt").end();


        }).activate().block();
    }

}
