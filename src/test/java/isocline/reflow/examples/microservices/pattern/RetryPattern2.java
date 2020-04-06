package isocline.reflow.examples.microservices.pattern;

import isocline.reflow.TestUtil;
import isocline.reflow.WorkEvent;
import isocline.reflow.FlowProcessor;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class RetryPattern2 {


    private Logger logger = LoggerFactory.getLogger(RetryPattern2.class);

    public void init() {
        logger.debug("init");
    }

    public void callService1(WorkEvent e) {
        logger.debug("Service1 - start");
        TestUtil.waiting(500);
        logger.debug("Service1 - end");

        e.origin().put("result:service1", "A");
        throw new RuntimeException("zzzz");
    }


    public void finish(WorkEvent e) {
        logger.debug("inactive start " + Thread.currentThread().getId());

        logger.debug(">"+e.origin());
        logger.debug(">"+e.origin().get("result:service1"));

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


    public void onError2(WorkEvent e) {

        logger.debug("error2 " + e.getEventName());

        Throwable err = e.getThrowable();
        if (err != null) {
            err.printStackTrace();
        }
    }

    public static int count = 0;


    @Test
    public void startTest() {


        FlowProcessor.core()
                .reflow(flow -> {

                    flow

                            .accept(this::callService1)
                            .retryOnError(3, 2000);


                }).run();


        TestUtil.waiting(10000);
    }
}
