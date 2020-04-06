package isocline.reflow;


import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class TestService extends TestBase {

    private Logger logger = LoggerFactory.getLogger(TestService.class);


    String extractor(WorkEvent e) {
        return "TEXT";
    }

    public int test1(String in) {
        return in.length();
    }

    String test2(int in) {
        return "SIZE:" + String.valueOf(in);
    }

    void result(String txt) {
        Assert.assertEquals("SIZE:4", txt);
        logger.debug("result = [" + txt + "]");
    }

    AtomicInteger count = new AtomicInteger(0);

    void exec1(WorkEvent e) {
        count.addAndGet(1);
        logger.debug("execute 1 [start]");
        TestUtil.waiting(50);
        logger.debug("execute 1 [end]");
        count.addAndGet(-1);
    }

    void exec2(WorkEvent e) {
        count.addAndGet(1);
        logger.debug("execute 2 [start]");
        TestUtil.waiting(50);
        logger.debug("execute 2 [end]");
        count.addAndGet(-1);
    }

}
