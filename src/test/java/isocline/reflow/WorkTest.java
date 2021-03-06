package isocline.reflow;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;


public class WorkTest extends TestBase {

    private static Logger logger = LoggerFactory.getLogger(WorkTest.class);


    private FlowProcessor flowProcessor;


    @After
    public void after() {

        //FlowProcessor.core().shutdown(3000);
    }

    @Test
    public void executeSimple() throws Exception {


        Re.flow(e -> {

            int seq= getCounter("executeSimple").addAndGet(1);

            logger.debug("exec " + seq);

            return Work.TERMINATE;
        }).activate().block();


        assertEquals(1, getCounter("executeSimple").get());

    }

    @Test
    public void executeSimple2() throws Exception {


        Re.flow(e -> {
            int seq=getCounter("executeSimple2").addAndGet(1);

            logger.debug("exec " + seq);

            return Work.TERMINATE;
        }).activate(e -> {
            logger.debug("exception "+e);
        }).block();


        assertEquals(1, getCounter("executeSimple2").get());

    }

    private int count = 0;

    @Test
    public void executeSimple3() throws Exception {

        count = 0;

        Re.peat(() -> {
            count++;
            logger.debug("Hello Re.flow ! ");
        })
                .interval(1000)
                .finishTimeFromNow(Time.SECOND * 3)
                .activate().block();

        Assert.assertEquals(3, count);
    }


    @Test
    public void executeSimple4() throws Exception {


        Re.flow((WorkEvent e) -> {

            getCounter("executeSimple4").addAndGet(1);
            logger.debug("Hello Re.flow ! "
                    + e.origin().get("z"));
            return Work.WAIT;
        })
                .on("_dummy")
                .daemonMode()
                .activate();


        Re.peat(() -> {
            System.out.println("FIRE");
            Re.quest("_dummy", e -> e.put("z", "zz").put("z", "sdf"));
        })
                .interval(1000)
                .initialDelay(2 * Time.SECOND)
                .finishTimeFromStart(5 * Time.SECOND)
                .activate().block();

        Assert.assertEquals(3, getCounter("executeSimple4").get());
    }


    @Test
    public void executeByEvent() throws Exception {


        Activity plan = Re.flow((WorkEvent event) -> {

            int seq = getCounter("executeByEvent").addAndGet(1);

            logger.debug("executeByEvent " + seq + " event:" + event.getEventName());

            return Work.WAIT;
        }, "testEvent").activate();


        Re.flow((WorkEvent event) -> {
            logger.debug("fire event:" + event.getEventName());

            event.getActivity().getFlowProcessor().emit("testEvent", event);

            return Work.TERMINATE;
        }).activate().block();

        plan.block(1000);


        assertEquals(1, getCounter("executeByEvent").get());

    }


    @Test
    public void executeOneTime() throws Exception {


        Plan plan = Re.flow((WorkEvent event) -> {
            int seq = getCounter("executeOneTime").addAndGet(1);
            logger.debug("exec " + seq);

            return Work.TERMINATE;
        });

        plan.activate().block(1000);


        assertEquals(1, getCounter("executeOneTime").get());

    }

    @Test
    public void executeSleep() throws Exception {

        Plan plan = Re.flow((WorkEvent event) -> {
            int seq=getCounter("executeSleep").addAndGet(1);
            logger.debug("executeSleep " + seq);

            return Work.WAIT;
        });

        plan.activate().block(300);

        assertEquals(1, getCounter("executeSleep").get());

    }

    @Test
    public void executeLoop() throws Exception {

        Plan plan = Re.flow((WorkEvent event) -> {
            int seq = getCounter("executeLoop").addAndGet(1);
            logger.debug("exec " + seq);

            if (seq == 10) {
                return Work.TERMINATE;
            }

            return Work.LOOP;
        });


        plan.activate().block(100);

        assertEquals(10, getCounter("executeLoop").get());

    }


    @Test
    public void executeRunnable() throws Exception {


        Runnable runnable = () -> {
            logger.debug("runnable");
            getCounter("executeRunnable").addAndGet(1);
        };


        Re.peat(runnable)
                .initialDelay(2 * Time.SECOND)
                .activate()
                .block();


        assertEquals(1, getCounter("executeRunnable").get());

    }

}
