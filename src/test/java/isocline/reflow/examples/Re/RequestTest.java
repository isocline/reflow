package isocline.reflow.examples.Re;

import isocline.reflow.FlowableWork;
import isocline.reflow.Re;
import isocline.reflow.TestUtil;
import isocline.reflow.WorkEvent;
import isocline.reflow.log.XLogger;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestTest {

    private XLogger logger = XLogger.getLogger(RequestTest.class);


    private long totalProcessTime = 200;

    private long methodCallCount = 5;


    public void testLoop(WorkEvent e) {
        //logger.debug("test2");
        TestUtil.waiting(totalProcessTime / methodCallCount);
        e.origin().put("price", Math.random());
    }


    private AtomicInteger count = new AtomicInteger(0);
    int testCount = 200;

    @Test
    public void testRequest() throws Exception {

        FlowableWork flowableWork = f -> {

            int seq = 0;
            while (methodCallCount > seq++) {
                f.next(this::testLoop);
            }

            f.end();
        };

        Re.flow(flowableWork)
                .on("chk")
                .daemonMode()
                .activate();

        Thread.sleep(1000);

        long t1 = System.currentTimeMillis();

        for (int i = 0; i < testCount; i++) {

            Re.quest("chk",
                    e -> {
                        e.put("ip", "192.168.0.1");
                    },
                    e -> {


                        double z = (double) e.get("price");
                        int c = count.addAndGet(1);
                        if (testCount <= c) {
                            long gap = System.currentTimeMillis() - t1;
                            logger.debug("process time:" + gap);
                        }
                        logger.debug(z + " " + c);
                    });
        }

        Thread.sleep(3000);

    }


    @Test
    public void testRequest2() throws Exception {

        FlowableWork flowableWork = f -> {

            int seq = 0;
            while (methodCallCount > seq++) {
                f.next(this::testLoop);
            }

            f.end();
        };

        Re.flow(flowableWork)
                .on("lxq://local/biz/chk")
                .daemonMode()
                .activate();

        TestUtil.waiting(1000);

        Re.quest("lxq://local/biz/chk", "proto").subscribe(e -> {

            double z = (double) e.get("price");
            logger.debug(z);
        }).block();


    }


    private void test1() {
        logger.debug("123");

    }

    private void push(WorkEvent e) {

        logger.debug("zzz");


    }

    @Test
    public void testRequestPush() throws Exception {

        FlowableWork flowableWork = f -> {

            f.next(this::test1);

            f.flag("push").next(this::push).fireEvent("push",1000);

            f.wait("end").end();

        };


        Re.call(flowableWork)
                .on("lxq://local/biz/chk")
                .daemonMode()
                .activate();

        TestUtil.waiting(1000);

        Re.quest("lxq://local/biz/chk", "proto").subscribe(e -> {


        }).block();



    }
}

