package isocline.reflow;

import isocline.reflow.log.XLogger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class WorkTest {

    private static XLogger logger = XLogger.getLogger(WorkTest.class);

    private int seq;

    private FlowProcessor flowProcessor;


    @After
    public void after() {

        //FlowProcessor.core().shutdown(3000);
    }

    @Test
    public void executeSimple() throws Exception {


        Re.task(e -> {
            seq++;
            logger.debug("exec " + seq);

            return Work.TERMINATE;
        }).activate().block();


        assertEquals(1, seq);

    }

    @Test
    public void executeSimple2() throws Exception {


        Re.task(e -> {
            seq++;
            logger.debug("exec " + seq);

            return Work.TERMINATE;
        }).activate(e -> {
            logger.debug(e);
        }).block();


        assertEquals(1, seq);

    }

    private int count = 0;

    @Test
    public void executeSimple3() throws Exception {

        count = 0;

        Re.task(() -> {
            count++;
            logger.debug("Hello Re.task ! ");
        })
                .interval(1000)
                .finishTimeFromNow(Time.SECOND * 3)
                .activate().block();

        Assert.assertEquals(3, count);
    }


    @Test
    public void executeSimple4() throws Exception {

        count = 0;

        Re.task((WorkEvent e) -> {
            count++;
            logger.debug("Hello Re.task ! "
                    + e.origin().get("z"));
            return Work.WAIT;
        })
                .on("test")
                .daemonMode()
                .activate();


        Re.task(() -> {
            System.out.println("FIRE");
            Re.quest("test", e -> e.put("z", "zz").put("z", "sdf"));
        })
                .interval(1000)
                .initialDelay(2 * Time.SECOND)
                .finishTimeFromStart(5 * Time.SECOND)
                .activate().block();

        Assert.assertEquals(3, count);
    }


    @Test
    public void executeByEvent() throws Exception {

        Activity plan = Re.task((WorkEvent event) -> {
            seq++;
            logger.debug("exec " + seq + " event:" + event.getEventName());

            return Work.WAIT;
        }, "testEvent").activate();


        Re.task((WorkEvent event) -> {
            logger.debug("fire event:" + event.getEventName());

            event.getActivity().getFlowProcessor().emit("testEvent", event);

            return Work.TERMINATE;
        }).activate().block();

        plan.block(1000);


        assertEquals(1, seq);

    }


    @Test
    public void executeOneTime() throws Exception {


        Plan plan = Re.task((WorkEvent event) -> {
            seq++;
            logger.debug("exec " + seq);

            return Work.TERMINATE;
        });

        plan.activate().block(1000);


        assertEquals(1, seq);

    }

    @Test
    public void executeSleep() throws Exception {

        Plan plan = Re.task((WorkEvent event) -> {
            seq++;
            logger.debug("exec " + seq);

            return Work.WAIT;
        });

        plan.activate().block(100);

        assertEquals(1, seq);

    }

    @Test
    public void executeLoop() throws Exception {

        Plan plan = Re.task((WorkEvent event) -> {
            seq++;
            logger.debug("exec " + seq);

            if (seq == 10) {
                return Work.TERMINATE;
            }

            return Work.LOOP;
        });


        plan.activate().block(100);

        assertEquals(10, seq);

    }


    @Test
    public void executeRunnable() throws Exception {

        seq = 0;
        Runnable runnable = () -> {
            logger.debug("runnable");
            seq++;
        };


        Re.task(runnable)
                .initialDelay(2 * Time.SECOND)
                .activate()
                .block();


        assertEquals(1, seq);

    }

}
