package isocline.reflow;

import isocline.reflow.log.XLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class WorkTest {

    private static XLogger logger = XLogger.getLogger(WorkTest.class);

    private int seq;

    private FlowProcessor flowProcessor;

    @Before
    public void before() {
        seq = 0;

        flowProcessor = FlowProcessorFactory.getProcessor();
    }

    @After
    public void after() {

        flowProcessor.shutdown(1000);
    }

    @Test
    public void executeSimple() throws Exception {


        FlowProcessor.core()
                .reflow(e -> {
                    seq++;
                    logger.debug("exec " + seq);

                    return Work.TERMINATE;
                }).activate().block();


        assertEquals(1, seq);

    }

    @Test
    public void executeSimple2() throws Exception {


        FlowProcessor.core()
                .reflow(e -> {
                    seq++;
                    logger.debug("exec " + seq);

                    return Work.TERMINATE;
                }).activate(e->{
                    logger.debug(e);
        }).block();


        assertEquals(1, seq);

    }

    @Test
    public void executeByEvent() throws Exception {

        ActivatedPlan plan =

                flowProcessor.reflow((WorkEvent event) -> {
                    seq++;
                    logger.debug("exec " + seq + " event:" + event.getEventName());

                    return Work.WAIT;
                }, "testEvent").activate();


        flowProcessor.execute((WorkEvent event) -> {
            logger.debug("fire event:" + event.getEventName());

            event.getPlan().getFlowProcessor().emit("testEvent", event);

            return Work.TERMINATE;
        });

        plan.block(1000);


        assertEquals(1, seq);

    }


    @Test
    public void executeOneTime() throws Exception {


        Plan plan =

                flowProcessor.reflow((WorkEvent event) -> {
                    seq++;
                    logger.debug("exec " + seq);

                    return Work.TERMINATE;
                });

        plan.activate().block(1000);


        assertEquals(1, seq);

    }

    @Test
    public void executeSleep() throws Exception {

        Plan plan =

                flowProcessor.reflow((WorkEvent event) -> {
                    seq++;
                    logger.debug("exec " + seq);

                    return Work.WAIT;
                });

        plan.activate().block(100);

        assertEquals(1, seq);

    }

    @Test
    public void executeLoop() throws Exception {

        Plan plan =

                flowProcessor.reflow((WorkEvent event) -> {
                    seq++;
                    logger.debug("exec " + seq);

                    if (seq == 10) {
                        return Work.TERMINATE;
                    }

                    return Work.LOOP;
                });


        plan.activate().block(100);

        assertEquals(10, seq);

    }


    @Test
    public void executeRunnable() throws Exception {

        seq = 0;
        Runnable runnable = ()-> {
            logger.debug("runnable");
            seq++;
        };


        FlowProcessor.core()
                .reflow(runnable)
                .startDelayTime(2*Clock.SECOND)
                .activate()
                .block();


        assertEquals(1, seq);

    }

}
