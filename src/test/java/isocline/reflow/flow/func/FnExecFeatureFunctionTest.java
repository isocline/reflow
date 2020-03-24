package isocline.reflow.flow.func;

import isocline.reflow.FlowProcessor;
import isocline.reflow.Re;
import isocline.reflow.TestUtil;
import isocline.reflow.WorkEvent;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FnExecFeatureFunctionTest {

    private Logger logger = LoggerFactory.getLogger(FnExecFeatureFunction.class);


    private void slowMethod() {

        logger.debug("_dummy");
        TestUtil.waiting(5000);
        throw new RuntimeException("err");
    }

    private int seq = 0;

    private void delaySerice() throws Exception {

        seq++;
        logger.debug("START delaySerice");
        if (seq < 20) {
            Thread.sleep(3300);
            Assert.fail();
        }

        logger.debug("END delaySerice ");
    }

    private void catchEvent(WorkEvent e) {

        logger.debug("cap " + e.getEventName() + " " + e);

        Throwable err = e.getThrowable();
        if (err != null) {
            err.printStackTrace();
        }
    }


    @Test
    public void testTimeout() throws Exception {

        Re.flow(flow -> {
                    flow.next(this::delaySerice, e -> e.timeout(1000, "tt"));

                    flow.wait("tt").next((WorkEvent e) -> {
                        e.getThrowable().printStackTrace();
                    }).end();
                }

        ).activate().block();


    }

    @Test
    public void test1() throws Exception {

        FlowProcessor.core()
                .reflow(f -> f
                        .next(this::delaySerice, p ->
                                p.before("b1", "b2").success("s1", "s2").fail("f1").end("e1").timeout(3000))
                        .wait("b1", "b2", "s1", "s2", "f1", "e1").next(this::catchEvent)
                        .wait("x").end())

                .activate();

        FlowProcessor.core().shutdown(7000);
    }


    @Test
    public void testCircuitBreak() throws Exception {

        for (int i = 0; i < 80; i++) {
            Re.flow(flow -> {
                        flow.next(this::delaySerice, ft -> ft.timeout(1000, "tt").circuitBreak("uniqId", 3, 1000));

                        flow.onError("*").next((WorkEvent e) -> {
                            e.getThrowable().printStackTrace();
                        }).end();
                    }


            ).activate();
            TestUtil.waiting(50);
        }


    }
}
