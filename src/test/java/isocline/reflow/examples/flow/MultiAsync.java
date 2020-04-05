package isocline.reflow.examples.flow;

import isocline.reflow.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MultiAsync implements FlowableWork {


    private static Logger logger = LoggerFactory.getLogger(MultiAsync.class);

    private List<Long> list = Collections.synchronizedList(new ArrayList());


    public void asyncMulti() {
        logger.debug("** invoke - asyncMulti");
        long calc = 100 + (long)(1000*Math.random());
        TestUtil.waiting(calc);
        list.add(calc);
        logger.debug("** invoke - async1 - END "+calc);
    }


    public void sum(WorkEvent event) {
        long sum = list.stream().mapToLong(x->x).sum();
        logger.debug("** invoke - sum - result:"+sum);
    }




    public void defineWorkFlow(WorkFlow flow) {
        flow.runAsync(this::asyncMulti,5)
                .waitAll()
                .accept(this::sum);
    }


    @Test
    public void test() throws InterruptedException {
        Activity plan = start();

        plan.block();

        FlowProcessorFactory.getProcessor().awaitShutdown();


    }


}
