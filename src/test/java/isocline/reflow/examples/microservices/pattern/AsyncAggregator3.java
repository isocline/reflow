package isocline.reflow.examples.microservices.pattern;

import isocline.reflow.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class AsyncAggregator3 implements FlowableWork {


    private Logger logger = LoggerFactory.getLogger(AsyncAggregator3.class);

    public void init() {
        logger.debug("init");
    }

    public void callService1(WorkEvent e) {
        logger.debug("Service1 - start");
        TestUtil.waiting(2000);
        logger.debug("Service1 - end");

        e.origin().put("result:service1", "A");
    }


    public void callService2(WorkEvent e) {
        logger.debug("Service2 - start "+Thread.currentThread().getId());
        TestUtil.waiting(3000);
        logger.debug("Service2 - end");
        e.origin().put("result:service2", "B");
    }

    public void callService3(WorkEvent e) {
        logger.debug("Service3 - start");
        TestUtil.waiting(1000);
        logger.debug("Service3 - end");
        e.origin().put("result:service3", "C");
    }

    public void finish(WorkEvent e) {
        logger.debug("inactive start "+Thread.currentThread().getId());

        logger.debug("> "+e.origin());
        logger.debug("> "+e.origin().get("result:service1"));

        String result = e.origin().get("result:service1").toString()
                + e.origin().get("result:service2")
                + e.origin().get("result:service3");

        assertEquals("ABC", result);

        logger.debug("inactive - " + result);
    }


    public void onTimeout(WorkEvent e) {
        logger.debug("timeout  " + e.getEventName());
    }

    public void onError(WorkEvent e) {

        logger.debug("error " + e.getEventName());

        Throwable err = e.getThrowable();
        if (err != null) {
            err.printStackTrace();
        }
    }

    @Override
    public void defineWorkFlow(WorkFlow flow) {

        flow.runAsync(this::callService1, "p1")
                .runAsync(this::callService2, "p2")
                .runAsync(this::callService3, "p3");

        flow.waitAll("p1", "p2", "p3").accept(this::finish).end();


        flow.onError("*").accept(this::onError);
        flow.wait("timeout").accept(this::onTimeout).end();

    }


    @Test
    public void startTest() {
        FlowProcessor processor = FlowProcessorFactory.getProcessor();

        processor.execute(new AsyncAggregator3());

        processor.awaitShutdown();
    }
}
