package isocline.reflow.flow;

import isocline.reflow.Re;
import isocline.reflow.TestUtil;
import isocline.reflow.WorkEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CalcWorkFlowTest {

    Logger logger = LoggerFactory.getLogger(CalcWorkFlowTest.class);

    public int getDouble(int x) {
        System.err.println(x);
        TestUtil.waiting(100 + (long) (100 * Math.random()));
        System.err.println(">" + x);
        return x * 2;
    }


    public void check(Object data) {
        logger.debug(" sum  --> result=" + data);
    }

    public void error(WorkEvent e) {
        System.err.println("---");
        System.err.println(e.getThrowable());
    }


    @Test
    public void test() {
        Re.flow(f -> {
            f
                    .extractAsync(e -> getDouble(2))
                    .extractAsync(e -> getDouble(3))
                    .waitAll()
                    .apply(e -> e.getIntStream().sum())
                    .onError(this::error).end();


        }).activate(this::check).block();
    }
}
