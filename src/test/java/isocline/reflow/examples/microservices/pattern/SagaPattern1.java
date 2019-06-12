package isocline.reflow.examples.microservices.pattern;

import isocline.reflow.FlowProcessor;
import isocline.reflow.WorkEvent;
import isocline.reflow.log.XLogger;
import org.junit.Test;


/**
 *
 *
 * https://www.youtube.com/watch?v=YPbGW3Fnmbc
 *
 */
public class SagaPattern1 {

    private static int CNT = 0;

    private XLogger logger = XLogger.getLogger(SagaPattern1.class);

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
                            .next(this::callSvc1, "s1")
                            .next(this::callSvc2, "s2")
                            .next(this::callSvc3, "s3")
                            .finish();


                    f.onError("s3").runAsync(this::compensateSvc3,this::compensateSvc2,this::compensateSvc1).finish();
                    f.onError("s2").runAsync(this::compensateSvc2,this::compensateSvc1).finish();
                    f.onError("s1").next(this::compensateSvc1).finish();


                }).activate().block();

    }


}
