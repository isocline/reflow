package isocline.reflow.examples.flow;

import isocline.reflow.FlowProcessorFactory;
import isocline.reflow.FlowableWork;
import isocline.reflow.TestUtil;
import isocline.reflow.WorkFlow;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderProcess implements FlowableWork {

    private static Logger logger = LoggerFactory.getLogger(OrderProcess.class);


    private String id = "autoExpress";


    public OrderProcess() {

    }


    private void writeLog() {

        logger.debug(id + " writeLog");

    }


    private void record() {
        logger.debug(id + " record");
    }


    public void checkStock() {
        logger.debug(id + " checkStock");

        TestUtil.waiting(2000);

        logger.debug(id + " checkStock end");

    }


    private void checkSupplier() {

        logger.debug(id + " checkSupplier");

        TestUtil.waiting(3000);

        logger.debug(id + " checkSupplier end");

    }


    private void checkUserPoint() {
        logger.debug(id + " checkUserPoint");

    }


    private void makeMessage() {
        logger.debug(id + " makeMessage");
    }


    private void recordUserInfo() {
        logger.debug(id + " recordUserInfo");
    }


    public void defineWorkFlow(WorkFlow flow) {

        flow
                .runAsync(this::writeLog)
                .runAsync(this::record)
                .next(this::checkStock, "checkStock")
                .next(this::checkSupplier, "checkSup");

        flow
                .wait("checkStock&checkSup").next(this::makeMessage).end();


    }

    @Test
    public void startTest() {
        OrderProcess process = new OrderProcess();
        process.start();

        FlowProcessorFactory.getProcessor().shutdown(10000);


    }


}
