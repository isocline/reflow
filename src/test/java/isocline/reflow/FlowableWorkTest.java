package isocline.reflow;

import isocline.reflow.log.XLogger;
import isocline.reflow.module.WorkEventGenerator;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;

public class FlowableWorkTest {

    private XLogger logger = XLogger.getLogger(FlowableWorkTest.class);

    public double getExhangeRate(double money, int formCountryCode, int toCountryCode) {

        logger.debug("call");

        TestUtil.waiting(1000 + (long) (1000 * Math.random()));

        double result = money + money * (formCountryCode / toCountryCode);

        logger.debug(money + "  --> result=" + result);
        return result;
    }


    public void check(Object data) {
        logger.debug( " sum  --> result=" + data);
    }


    @Test
    public void testBasic() {

        FlowProcessor.core().reflow(f -> {
            f
                    .applyAsync(e -> getExhangeRate(1000, 3, 5))
                    .applyAsync(e -> getExhangeRate(2000, 4, 2))
                    .applyAsync(e -> getExhangeRate(5000, 3, 4))
                    .waitAll().next((WorkEvent e) -> e.getDoubleStream().sum());
        }).activate(this::check).block();

    }


    @Test
    public void testBasic2() throws Exception {
        AtomicInteger count = new AtomicInteger(0);

        FlowProcessor.core().reflow(f -> {
            f
                    .applyAsync(e -> getExhangeRate(1000, 3, 5))
                    .applyAsync(e -> getExhangeRate(2000, 4, 2))
                    .applyAsync(e -> getExhangeRate(5000, 3, 4))
                    .waitAll().next((WorkEvent e) -> e.getDoubleStream().sum()).next((WorkEvent e) -> {
                count.addAndGet(1);
                System.err.println("RESULT = " + e.origin().getResult());
            });
        })
                .daemonMode()
                .on("xxx")
                .activate(logger::debug);

        WorkEventGenerator generator = new WorkEventGenerator();
        generator.setEventName("xxx");
        generator.setRepeatTime(100);

        FlowProcessor.core().reflow(generator).startDelayTime(2 * Clock.SECOND).finishTimeFromNow(7 * Clock.SECOND).activate();



        logger.debug("END ======");
        for(int i=0;i<100;i++) {
            int crntCount = count.get();
            logger.debug(crntCount+"/"+generator.getCount());

            if(generator.getCount()!=0 && crntCount==generator.getCount()) {
                return;
            }
            Thread.sleep(500);
        }


        fail();



    }
}

