package isocline.reflow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;


public class PlanTest {

    private static Logger logger = LoggerFactory.getLogger(PlanTest.class);

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


        processor.task((WorkEvent event) -> {
            seq++;
            logger.debug("exec " + seq);

            return Work.TERMINATE;
        }).activate().block(10);




        assertEquals(1, seq);

    }

    @Test
    public void executeSleep() throws Exception {


        processor.task((WorkEvent event) -> {
            seq++;
            logger.debug("exec " + seq);

            return Work.WAIT;
        }).activate().block(10);


        assertEquals(1, seq);

    }

    @Test
    public void executeLoop() throws Exception {


        processor.task((WorkEvent event) -> {
            seq++;
            logger.debug("exec " + seq);

            if (seq == 10) {
                return Work.TERMINATE;
            }

            return Work.LOOP;
        }).activate().block(10);


        assertEquals(10, seq);

    }


}
