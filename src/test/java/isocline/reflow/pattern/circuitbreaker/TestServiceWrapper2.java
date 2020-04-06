package isocline.reflow.pattern.circuitbreaker;

import isocline.reflow.Re;
import isocline.reflow.flow.CustomWorkFlow;
import isocline.reflow.flow.pattern.CircuitBreaker;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestServiceWrapper2 {

    private Logger logger = LoggerFactory.getLogger(TestServiceWrapper2.class);

    public void test() {

        logger.debug("STRT >");


        TestService svc = new TestService();

        Re.flow(flow -> {
            CustomWorkFlow $ = flow.applyPattern(CircuitBreaker.init("test", $2->{
                $2.setMaxFailCount(3);
            }));

            flow.run(svc::executeUnstalbe);

            $.closePattern();


        }).activate();


    }


    @Test
    public void testMulti() {
        Re.peat(this::test).interval(true,500).finishTimeFromStart(1000*60).activate().block();


    }
}
