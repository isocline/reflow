package isocline.reflow;

import isocline.reflow.log.XLogger;
import org.junit.Test;

public class FlowableWorkTest {

    private XLogger logger = XLogger.getLogger(FlowableWorkTest.class);

    public double getExhangeRate(double money, int formCountryCode, int toCountryCode) {

        logger.debug("call");

        TestUtil.waiting(1000 + (long) (1000 * Math.random()));

        double result = money + money * (formCountryCode / toCountryCode);

        logger.debug(money+ "  --> result=" + result);
        return result;
    }


    @Test
    public void testBasic() {
        WorkProcessor.main().reflow(f -> {
            f
                    .applyAsync(e -> getExhangeRate(1000, 3, 5))
                    .applyAsync(e -> getExhangeRate(2000, 4, 2))
                    .applyAsync(e -> getExhangeRate(5000, 3, 4))
                    .waitAll().next((WorkEvent e) -> e.getDoubleStream().sum());
        }).activate(logger::debug).block();


    }

}

