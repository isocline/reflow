package isocline.reflow.pattern;

import isocline.reflow.*;
import isocline.reflow.flow.WorkFlowFactory;
import isocline.reflow.log.XLogger;
import org.junit.Test;

import static org.junit.Assert.fail;

public class SagaTest extends TestBase {


    private XLogger logger = XLogger.getLogger(SagaTest.class);

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
        fail();
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

        WorkFlow f = WorkFlowFactory.create();

        f.next(this::init);

        Saga.apply(f, f2 -> {
            f2.transaction(this::callSvc1, this::compensateSvc1);
            f2.transaction(this::callSvc2, this::compensateSvc2);
            f2.transaction(this::callSvc3, this::compensateSvc3);

        });

        Re.flow(f).activate();

    }
}
