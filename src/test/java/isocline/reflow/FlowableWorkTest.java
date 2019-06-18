package isocline.reflow;

import isocline.reflow.event.WorkEventFactory;
import isocline.reflow.log.XLogger;
import isocline.reflow.module.WorkEventGenerator;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FlowableWorkTest {

    private XLogger logger = XLogger.getLogger(FlowableWorkTest.class);

    private int count4case0_result = 0;
    private int count4case0 = 0;
    private int count4case1 = 0;
    private int count4case2 = 0;


    /**
     * @param money
     * @param formCountryCode
     * @param toCountryCode
     * @return
     */
    public double getExhangeRate(double money, int formCountryCode, int toCountryCode) {

        logger.debug(money + " > call");

        TestUtil.waiting(500 + (long) (100 * Math.random()));

        double result = money + money * (formCountryCode / toCountryCode);

        logger.debug(money + " > result : " + result);
        return result;
    }


    public void check(Object data) {
        logger.debug(" sum  --> result=" + data);
    }


    private void case0(WorkEvent e) {
        count4case0++;
        logger.debug("invoke case0 " + e.origin().get("result"));

        if (e.origin().get("result") != null) {
            count4case0_result++;
        }
    }

    private void case1(WorkEvent e) {
        count4case1++;
        logger.debug("invoke case1");
    }

    private void case2() {
        count4case2++;
        logger.debug("invoke case2");
    }

    private void reset() {
        count4case0_result = 0;
        count4case0 = 0;
        count4case1 = 0;
        count4case2 = 0;
    }

    private void print() {
        System.out.println(count4case0_result);
        System.out.println(count4case0);
        System.out.println(count4case1);
        System.out.println(count4case2);
    }

    @Test
    public void testLooping() {
        reset();

        FlowProcessor.core()
                .reflow(f -> {
                    f

                            .next(this::case0)
                            .delay(100)
                            .fireEvent("s1", 0)
                            .wait("s1")
                            .next(this::case1)
                            .delay(200)
                            .next(this::case2)

                            .branch((WorkEvent event) -> {
                                if (event.count() > 5) {
                                    return "end";
                                }
                                return "s1";
                            });


                    f.wait("end").end();

                })
                .activate().block();

        print();

        assertEquals(this.count4case0, 1);
        assertEquals(this.count4case1, 6);
        assertEquals(this.count4case2, 6);


    }


    @Test
    public void testLooping2() {
        reset();

        Re.flow(f -> {
            f
                    .next(this::case0)
                    .delay(200)

                    .flag("s1")
                    .next(this::case1)
                    .delay(100)
                    .next(this::case2)

                    .branch((WorkEvent event) -> {
                        if (event.count() > 5) {
                            return "end";
                        }
                        return "s1";
                    })
                    .flag("end").end();

        })
                .activate().block();

        print();
        assertEquals(this.count4case0, 1);
        assertEquals(this.count4case1, 6);
        assertEquals(this.count4case2, 6);

    }

    @Test
    public void testBasic() {

        Re.flow(f -> {
            f
                    .applyAsync(e -> getExhangeRate(1000 * Math.random(), 3, 5))
                    .applyAsync(e -> getExhangeRate(2000, 4, 2))
                    .applyAsync(e -> getExhangeRate(5000 * Math.random(), 3, 4))
                    .waitAll()
                    .next((WorkEvent e) -> e.getDoubleStream().sum());
        })
                .activate(this::check).block();

    }


    @Test
    public void testAsyncCalcByExternalEvent() throws Exception {
        AtomicInteger count = new AtomicInteger(0);

        Re.flow(f -> {
            f
                    .applyAsync(e -> getExhangeRate(1000, 3, 5))
                    .applyAsync(e -> getExhangeRate(2000, 4, 2))
                    .applyAsync(e -> getExhangeRate(5000, 3, 4))
                    .waitAll()
                    .next((WorkEvent e) -> e.getDoubleStream().sum())
                    .next((WorkEvent e) -> {
                        count.addAndGet(1);
                        double result = (double) e.getResult();
                        logger.info("RESULT = " + result);
                    });
        })
                .daemonMode()
                .on("calc")
                .activate(logger::debug);

        WorkEventGenerator generator = new WorkEventGenerator("calc", 400);

        Re.task(generator)
                .startTime(Time.nextSecond())
                .finishTimeFromStart(3 * Time.SECOND)
                .strictMode()
                .activate();

        logger.debug("task define completed");

        long t1 = System.currentTimeMillis();
        Thread.sleep(2000);
        for (int i = 0; i < 40; i++) {
            int crntCount = count.get();

            logger.debug(crntCount + "/" + generator.getCount() + " " + (System.currentTimeMillis() - t1));

            if (crntCount > 3 && generator.getCount() != 0 && crntCount == generator.getCount()) {
                return;
            }
            Thread.sleep(500);
        }

        fail();
    }

    @Test
    public void testSelfRepeatFlow() throws Exception {
        AtomicInteger count = new AtomicInteger(0);

        FlowableWork flow = (f) -> {
            f

                    .applyAsync(e -> getExhangeRate(1000 * Math.random(), 3, 5))
                    .applyAsync(e -> getExhangeRate(2000 * Math.random(), 4, 2))
                    .applyAsync(e -> getExhangeRate(5000 * Math.random(), 3, 4))
                    .waitAll()
                    .next((WorkEvent e) -> e.getDoubleStream().sum())
                    .next((WorkEvent e) -> {
                        count.addAndGet(1);
                        logger.info("RESULT = " + e.getResult());
                    }).end();
        };


        Activity plan = Re.flow(flow)
                .interval(2000)
                .finishTimeFromNow(Time.SECOND * 7)
                .activate(logger::error);

        logger.debug("execute flow async");

        Thread.sleep(5000);

        logger.debug("terninate activity of flow ");
        plan.inactive();


    }


    /***
     * Test for event emiting frame external task process
     *
     * @throws Exception
     */
    @Test
    public void testEmitEventFromExternal() throws Exception {

        reset();

        Re.flow(f -> {
            f.next(this::case0);
            f.wait("case1").next(this::case1);
            f.wait("case2").next(this::case2);
        })
                .on("ev")
                .daemonMode()
                .activate();


        Re.task((WorkEvent e) -> {

            e.put("result", "skkim");

            WorkEvent e2 = WorkEventFactory.createOrigin("x").setFireEventName("case2");

            logger.debug("fire1 " + e);
            logger.debug("fire2 " + e2);
            e.getActivity().getFlowProcessor()

                    .emit("ex", e)
                    .emit("ev", e)
                    .emit("ev", e2)
                    .emit("ev", "case1", e2)
                    .emit("ev", "case2", e2);
            return Work.TERMINATE;
        })
                .activate().block();

        FlowProcessor.core()
                .shutdown(300);

        assertEquals(1, this.count4case0_result);
        assertEquals(2, this.count4case0);
        assertEquals(1, this.count4case1);
        assertEquals(1, this.count4case2);
    }

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


                        Double z = (Double) e.get("price");
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
}

