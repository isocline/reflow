package isocline.reflow.flow.func;

import isocline.reflow.FlowProcessor;
import isocline.reflow.TestUtil;
import isocline.reflow.WorkEvent;
import isocline.reflow.log.XLogger;
import org.junit.Test;

import static isocline.reflow.WorkHelper.Reflow;

public class FnExecFeatureFunctionTest {

    private XLogger logger = XLogger.getLogger(FnExecFeatureFunction.class);


    private void test() {

        logger.debug("test");
        TestUtil.waiting(5000);
        throw new RuntimeException("err");
    }

    private void test2() throws Exception {

        logger.debug("test2");
        Thread.sleep(3300);
        logger.debug("test2 end");
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

        Reflow(flow -> {
                    flow.next(this::test2, e -> e.timeout(3000, "tt"));

                    flow.wait("tt").next((WorkEvent e)->{e.getThrowable().printStackTrace();}).end();
                }

                        //.wait("tt").next((WorkEvent e)->e.getThrowable().printStackTrace()).end()
                //.wait("tt").next(this::catchEvent).end()
        );


    }

    @Test
    public void test1() throws Exception {

        FlowProcessor.core()
                .reflow(f -> f
                        .next(this::test2, exec ->
                                exec.before("b1", "b2").success("s1", "s2").fail("f1").end("e1").timeout(3000))
                        .wait("b1", "b2", "s1", "s2", "f1", "e1").next(this::catchEvent)
                        .wait("x").end())

                .activate();

        FlowProcessor.core().shutdown(7000);
    }
}
