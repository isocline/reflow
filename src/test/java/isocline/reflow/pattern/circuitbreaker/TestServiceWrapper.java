package isocline.reflow.pattern.circuitbreaker;

import isocline.reflow.Re;
import isocline.reflow.WorkEvent;
import org.junit.Test;

public class TestServiceWrapper {

    private static TestServiceWrapper instance;


    private int failCount = 0;

    private long lastFailDatetime = 0;

    private long timeout = 2000;



    static TestServiceWrapper getInstance() {
        if(instance==null) {
            instance = new TestServiceWrapper();
        }

        return instance;
    }



    public String isClosedCircuit(WorkEvent e) {


        if(failCount>10) {
            long timeGap = System.currentTimeMillis() - lastFailDatetime;

            if(timeGap > 1000*60 * 30) {
                failCount--;


                System.err.println("*1 CLOSE");
                return "close";

            }else if(timeGap > 1000*10) {

                if(timeGap%5==0) {
                    failCount--;

                    System.err.println("*2 CLOSE");
                    return "close";
                }
            }
        }else {
            System.err.println("*3 CLOSE");
            return "close";
        }

        System.err.println("* OPEN "+failCount);

        return "open";

    }





    public void chk(WorkEvent event) {
        failCount++;
        lastFailDatetime = System.currentTimeMillis();
        System.err.println("EEEEERRRRR > "+event.getFireEventName()+ " "+failCount);
    }

    public void open(WorkEvent event) {

        System.err.println("open > "+event.getFireEventName());
    }

    public void test() {

        System.err.println("STRT >");

        TestServiceWrapper wrapper = TestServiceWrapper.getInstance();

        TestService svc=  new TestService();

        Re.flow(flow -> {
            flow.branch(wrapper::isClosedCircuit);



            flow.wait("close").fireEvent("timeout",timeout).run(svc::executeUnstalbe).end();
            flow.wait("error::*").accept(wrapper::chk).end();
            flow.wait("timeout").accept(wrapper::chk).end();
            flow.wait("open").accept(wrapper::open).end();
        }).activate().block();


    }

    @Test
    public void test2() {

        Re.peat(this::test).interval(true,500).finishTimeFromStart(1000*60).activate().block();


    }
}
