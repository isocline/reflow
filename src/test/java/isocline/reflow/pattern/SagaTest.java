package isocline.reflow.pattern;

import isocline.reflow.Re;
import isocline.reflow.TestBase;
import isocline.reflow.WorkEvent;
import isocline.reflow.WorkFlow;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

public class SagaTest extends TestBase {


    private Logger logger = LoggerFactory.getLogger(SagaTest.class);

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

        Saga saga = Saga.init(conf -> {
            conf.setPrintError(false);
        });

        WorkFlow flow = WorkFlow.create();

        flow.next(this::init);

        saga.apply(flow, f2 -> {
            f2.transaction(this::callSvc1, this::compensateSvc1);
            f2.transaction(this::callSvc2, this::compensateSvc2);
            f2.transaction(this::callSvc3, this::compensateSvc3);

        });

        Re.flow(flow).activate();

    }


}
