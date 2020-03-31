package isocline.reflow.pattern;

import isocline.reflow.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircuitBreakerTest extends TestBase {

    private static int CNT = 0;

    private Logger logger = LoggerFactory.getLogger(CircuitBreakerTest.class);

    public void init() {
        logger.debug("init");
    }

    public void callService1(WorkEvent e) {
        CNT++;

        int seq = CNT;
        logger.debug("Service1 - start " + seq);
        TestUtil.waiting(100);
        logger.debug("Service1 - end " + seq);

        e.origin().put("result:service1", "A");

        if (seq > 1 && seq < 6) {
            logger.debug("Service1 - wait " + seq);
            TestUtil.waiting(3000);

            throw new RuntimeException("connect fail");
        }

    }


    public void onError(WorkEvent e) throws Throwable {

        logger.debug("error " + e.getEventName());

        Throwable err = e.getThrowable();
        if (err != null) {
            err.printStackTrace();
            throw err;
        } else {
            throw new RuntimeException("timeout");
        }
    }


    public static int count = 0;


    public void startTest() {

        WorkFlow flow = WorkFlow.create();

        CircuitBreaker.init(flow, conf -> {
            conf
                    .id("test")
                    .maxFailCount(3)
                    .timeout(100);


        }).apply(f -> {
            f.next(this::callService1);
        });

        Re.flow(flow).activate();

        WorkFlow f = WorkFlow.create();




    }


    @Test
    public void testMulti() {
        for (int i = 0; i < 10; i++) {

            CircuitBreakerTest test = new CircuitBreakerTest();
            try {
                test.startTest();
                TestUtil.waiting(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TestUtil.waiting(10000);
    }
}
