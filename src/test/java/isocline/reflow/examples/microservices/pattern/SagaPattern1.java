package isocline.reflow.examples.microservices.pattern;

import isocline.reflow.FlowProcessor;
import isocline.reflow.WorkEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 *
 * https://www.youtube.com/watch?v=YPbGW3Fnmbc
 *
 */
public class SagaPattern1 {

    private static int CNT = 0;

    private Logger logger = LoggerFactory.getLogger(SagaPattern1.class);

    public void init() {
        logger.debug("init");
    }

    public void callSvc1(WorkEvent e) {
        logger.debug("svc1");
    }

    public void callSvc2(WorkEvent e) {
        logger.debug("svc2");
        throw new RuntimeException("err");
    }


    public void callSvc3(WorkEvent e) {
        logger.debug("svc3");
    }


    public void compensateSvc1(WorkEvent e) {
        logger.debug("compensate svc1");
    }

    public void compensateSvc2(WorkEvent e) {
        logger.debug("compensate svc2");
    }

    public void compensateSvc3(WorkEvent e) {
        logger.debug("compensate svc3");
    }


    @Test
    public void startBasic() {

        FlowProcessor.core()
                .reflow(f -> {
                    f
                            .accept(this::callSvc1, "s1")
                            .accept(this::callSvc2, "s2")
                            .accept(this::callSvc3, "s3")
                            .end();


                    f.onError("s3").runAsync(this::compensateSvc3,this::compensateSvc2,this::compensateSvc1).end();
                    f.onError("s2").accept(this::compensateSvc2).accept(this::compensateSvc1).end();
                    f.onError("s1").accept(this::compensateSvc1).end();


                }).activate().block();

    }


}
