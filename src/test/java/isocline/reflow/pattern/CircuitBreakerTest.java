package isocline.reflow.pattern;

import isocline.reflow.*;
import isocline.reflow.log.XLogger;
import org.junit.Test;

public class CircuitBreakerTest extends TestBase {

    private static int CNT = 0;

    private XLogger logger = XLogger.getLogger(CircuitBreakerTest.class);

    public void init() {
        logger.debug("init");
    }

    public void callService1(WorkEvent e) {
        CNT++;

        logger.debug("Service1 - start " + CNT);
        TestUtil.waiting(100);
        logger.debug("Service1 - end " + CNT);

        e.origin().put("result:service1", "A");

        if (CNT > 2 && CNT < 7) {
            logger.debug("Service1 - wait " + CNT);
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
                    .timeout(500);


        }).apply(f -> {
            f.next(this::callService1);
        });

        Re.flow(flow).activate();


    }


    @Test
    public void testMulti() {
        for (int i = 0; i < 10; i++) {

            CircuitBreakerTest test = new CircuitBreakerTest();
            try {
                test.startTest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
