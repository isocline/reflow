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


        FlowProcessor.main()
                .reflow(e -> {
                    seq++;
                    logger.debug("exec " + seq);

                    return Work.TERMINATE;
                }).activate().block();


        assertEquals(1, seq);

    }

    @Test
    public void executeSimple2() throws Exception {


        FlowProcessor.main()
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

        Plan schedule =

                flowProcessor.reflow((WorkEvent event) -> {
                    seq++;
                    logger.debug("exec " + seq + " event:" + event.getEventName());

                    return Work.WAIT;
                }, "testEvent").activate();

        flowProcessor.execute((WorkEvent event) -> {
            logger.debug("fire event:" + event.getEventName());

            event.getPlan().getFlowProcessor().raiseEvent("testEvent", event);

            return Work.TERMINATE;
        });

        schedule.block(1000);


        assertEquals(1, seq);

    }


    @Test
    public void executeOneTime() throws Exception {


        Plan schedule =

                flowProcessor.reflow((WorkEvent event) -> {
                    seq++;
                    logger.debug("exec " + seq);

                    return Work.TERMINATE;
                }).activate();

        schedule.block(1000);


        assertEquals(1, seq);

    }

    @Test
    public void executeSleep() throws Exception {

        Plan schedule =

                flowProcessor.reflow((WorkEvent event) -> {
                    seq++;
                    logger.debug("exec " + seq);

                    return Work.WAIT;
                }).activate();

        schedule.block(100);

        assertEquals(1, seq);

    }

    @Test
    public void executeLoop() throws Exception {

        Plan schedule =

                flowProcessor.reflow((WorkEvent event) -> {
                    seq++;
                    logger.debug("exec " + seq);

                    if (seq == 10) {
                        return Work.TERMINATE;
                    }

                    return Work.LOOP;
                }).activate();


        schedule.block(100);
        assertEquals(10, seq);

    }


}
