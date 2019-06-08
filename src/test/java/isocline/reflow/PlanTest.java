package isocline.reflow;

import isocline.reflow.log.XLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class PlanTest {

    private static XLogger logger = XLogger.getLogger(PlanTest.class);

    private int seq;

    private FlowProcessor processor;

    @Before
    public void before() {
        seq = 0;

        processor = FlowProcessorFactory.getProcessor();
    }

    @After
    public void after() {

        processor.shutdown(1000);
    }

    @Test
    public void executeOneTime() throws Exception {


        processor.reflow((WorkEvent event) -> {
            seq++;
            logger.debug("exec " + seq);

            return Work.TERMINATE;
        }).activate();

        Thread.sleep(100);


        assertEquals(1, seq);

    }

    @Test
    public void executeSleep() throws Exception {


        processor.reflow((WorkEvent event) -> {
            seq++;
            logger.debug("exec " + seq);

            return Work.WAIT;
        }).activate();

        Thread.sleep(100);

        assertEquals(1, seq);

    }

    @Test
    public void executeLoop() throws Exception {


        processor.reflow((WorkEvent event) -> {
            seq++;
            logger.debug("exec " + seq);

            if (seq == 10) {
                return Work.TERMINATE;
            }

            return Work.LOOP;
        }).activate();


        Thread.sleep(100);
        assertEquals(10, seq);

    }


}
