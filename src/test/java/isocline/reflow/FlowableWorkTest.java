package isocline.reflow;

import isocline.reflow.event.WorkEventFactory;
import isocline.reflow.log.XLogger;
import isocline.reflow.module.WorkEventGenerator;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;

public class FlowableWorkTest {

    private XLogger logger = XLogger.getLogger(FlowableWorkTest.class);


    /**
     *
     * @param money
     * @param formCountryCode
     * @param toCountryCode
     * @return
     */
    public double getExhangeRate(double money, int formCountryCode, int toCountryCode) {

        logger.debug(money+ " > call");

        TestUtil.waiting(500 + (long) (100 * Math.random()));

        double result = money + money * (formCountryCode / toCountryCode);

        logger.debug(money + " > result : " + result);
        return result;
    }


    public void check(Object data) {
        logger.debug(" sum  --> result=" + data);
    }


    private void case1(WorkEvent e) {
        logger.debug("case1 >>" +e.get("result"));
    }

    private void case2() {
        logger.debug("case2");
    }

    @Test
    public void testBasic() {

        FlowProcessor.core().reflow(f -> {
            f
                    .applyAsync(e -> getExhangeRate(1000*Math.random(), 3, 5))
                    .applyAsync(e -> getExhangeRate(2000, 4, 2))
                    .applyAsync(e -> getExhangeRate(5000*Math.random(), 3, 4))
                    .waitAll().next((WorkEvent e) -> e.getDoubleStream().sum());
        }).activate(this::check).block();

    }


    @Test
    public void testBasic2() throws Exception {
        AtomicInteger count = new AtomicInteger(0);

        FlowProcessor.core().reflow(f -> {
            f
                    .applyAsync(e -> getExhangeRate(1000*Math.random(), 3, 5))
                    .applyAsync(e -> getExhangeRate(2000*Math.random(), 4, 2))
                    .applyAsync(e -> getExhangeRate(5000*Math.random(), 3, 4))
                    .waitAll().next((WorkEvent e) -> e.getDoubleStream().sum()).next((WorkEvent e) -> {
                count.addAndGet(1);
                logger.info("RESULT = " + e.getResult());
            });
        })
                .daemonMode()
                .on("xxx")
                .activate(logger::debug);

        WorkEventGenerator generator = new WorkEventGenerator();
        generator.setEventName("xxx");
        generator.setRepeatTime(10);

        FlowProcessor.core()
                .reflow(generator)
                .startTime(Clock.nextSecond())
                .finishTimeFromStart(2 * Clock.SECOND)
                .strictMode()
                .activate();


        logger.debug("END ======");
        long t1 = System.currentTimeMillis();
        Thread.sleep(2000);
        for (int i = 0; i < 200; i++) {
            int crntCount = count.get();

            logger.debug(crntCount + "/" + generator.getCount() +" " +(System.currentTimeMillis()-t1));

            if (generator.getCount() != 0 && crntCount == generator.getCount()) {
                return;
            }
            Thread.sleep(500);
        }

        fail();
    }


    @Test
    public void testBasic3() throws Exception {
        FlowProcessor.core().reflow(f-> {
            f.wait("case1").next(this::case1);
            f.wait("case2").next(this::case2);
        }).on("x").daemonMode().activate();


        FlowProcessor.core().reflow((WorkEvent e) ->{

            e.setFireEventName("case1");
            e.put("result","skkim");

            e.getPlan().getFlowProcessor()

                    .emit("x",e)
                    .emit("x", WorkEventFactory.createOrigin("x").setFireEventName("case2"));


            return Work.TERMINATE;
        }).startDelayTime(2*Clock.SECOND).activate().block();

        Thread.sleep(3000);
    }

}

