package isocline.reflow.pattern.circuitbreaker;

import isocline.reflow.TestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestService {

    private int sequence = 0;

    private static int seq=0;

    public TestService() {
        seq++;
        this.sequence = seq;
    }

    private Logger logger = LoggerFactory.getLogger(TestService.class);



    public void executeUnstalbe() {
        logger.debug("start "+sequence);

        if(sequence >1 && sequence <8) {
            throw new RuntimeException("zz");
        }

        if(sequence >7 && sequence <14) {
            logger.debug("wait "+sequence);
            TestUtil.waiting(10000);
        }

        logger.debug("end "+sequence);

    }
}
