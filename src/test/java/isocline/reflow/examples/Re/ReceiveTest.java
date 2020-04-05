package isocline.reflow.examples.Re;

import isocline.reflow.*;
import isocline.reflow.event.WorkEventFactory;
import isocline.reflow.module.PubSubBroker;
import isocline.reflow.module.WorkEventGenerator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ReceiveTest {


    private Logger logger = LoggerFactory.getLogger(ReceiveTest.class);

    private Map<String, WorkEvent> eventMap = new HashMap<>();

    private WorkEvent[] workEvents = null;

    private WorkEvent[] workEvents2 = null;

    private Collection<WorkEvent> collection;

    private Stream<WorkEvent> stream;

    private int pararrelSize = 4;

    private int realPararrelSize = 0;

    private int minUnitSize = 1;

    private boolean isNeedMakeArray = false;

    private AtomicInteger seqCounter = new AtomicInteger();

    public int getPararrelSize() {
        return this.pararrelSize;
    }


    private void receiveInit(WorkEvent event) {

        if (isNeedMakeArray) {
            makeWorkEventArray();
        }

        if (workEvents.length == 0) {
            realPararrelSize = 0;
            return;
        }


        workEvents2 = workEvents;
        seqCounter.set(-1);

        realPararrelSize = (workEvents2.length - 1) / minUnitSize + 1;
        if (realPararrelSize > pararrelSize) {
            realPararrelSize = pararrelSize;
        }

        logger.info("real==" + realPararrelSize);


    }

    private void receive(WorkEvent event) {

        int seq = this.seqCounter.addAndGet(1);
        if (seq >= realPararrelSize) {
            return;
        }

        logger.debug("1 push " + eventMap.size() + " " + seq);

        for (int i = (0 + seq); i < workEvents2.length; i = i + realPararrelSize) {
            logger.debug("S ====> " + eventMap.size() + " " + Thread.currentThread().getName() + " " + i);
            try {
                workEvents2[i].callback(event);
            } catch (Throwable e) {
                e.printStackTrace();
                String id = (String) workEvents2[i].get("callback_regist_id");
                eventMap.remove(id);
                isNeedMakeArray = true;


            }
            logger.debug("E ====> " + eventMap.size() + " " + Thread.currentThread().getName() + " " + i + " " + workEvents2.length);
        }


        logger.debug("2 push " + eventMap.size());

    }

    private synchronized void regist(WorkEvent event) {

        WorkEvent origin = event.origin();

        String id = (String) origin.get("id");

        origin.put("callback_regist_id", id);


        eventMap.put(id, origin);

        makeWorkEventArray();

        logger.debug("regist:" + id + " size= " + this.workEvents.length);

    }

    private void makeWorkEventArray() {

        isNeedMakeArray = false;
        this.collection = eventMap.values();
        this.workEvents = this.collection.toArray(new WorkEvent[collection.size()]);

    }

    private void unregist(WorkEvent event) {

    }


    @Test
    public void testBasic() {

        Re.flow(f -> {
            f.accept(this::receiveInit).runAsync(this::receive, this.getPararrelSize()).end();
            //f.apply(this::receive).end();

            f.wait("regist").accept(this::regist).end();

        }).on("rcv").daemonMode().activate();

        WorkEventGenerator generator = new WorkEventGenerator();
        generator.setEventName("rcv");

        Re.flow(generator).interval(200 , 500).strictMode().activate();


        WorkEvent e = WorkEventFactory.createOrigin().subscribe(event -> {
            logger.debug("###### - 1 " + " " + Thread.currentThread().getName());
            TestUtil.waiting(300);
            logger.debug("###### - 2 ------ END" + " " + Thread.currentThread().getName());
        });
        e.setFireEventName("regist");
        e.put("id", "xx");

        FlowProcessor.core().emit("rcv", "regist", e);


        e = WorkEventFactory.createOrigin().subscribe(event -> {
            //logger.debug("XX - 1 "+" "+Thread.currentThread().getName() );
            TestUtil.waiting(1);
            //logger.debug("XX - 2 END");
        });
        e.setFireEventName("regist");
        e.put("id", "xx2");

        FlowProcessor.core().emit("rcv", "regist", e);


        TestUtil.waiting(3000);


    }


    @Test
    public void testBasic2() {

        Re.flow(new PubSubBroker()).on("rcv").daemonMode().activate();


        // publisher
        WorkEventGenerator generator = new WorkEventGenerator();
        generator.setEventName("rcv");

        //Re.flow(generator).interval(500, 200).strictMode().activate();
        Re.flow(e -> {


            WorkEvent newEvent = e;
            newEvent.put("x", "zz");

            e.getActivity().getFlowProcessor().emit("rcv", newEvent);
            logger.debug("FIRE " + e.hashCode());
            return Work.WAIT;
        }).interval(500, 10).activate();


        // subscribe

        Re.ceive("rcv", "regist", e -> e.put("id", "jj"))
                .filter(e -> true)
                .subscribe(event -> {
                    logger.debug("Re.ceive >" + event.get("x"));
                });


        WorkEvent e = WorkEventFactory.createOrigin().filter(ee -> {
            return false;
        }).subscribe(event -> {

            logger.debug("###### - 2 ------ END" + " " + Thread.currentThread().getName() + " " + event.get("x"));
            TestUtil.waiting(100);
        });
        e.setFireEventName("regist");
        e.put("id", "xx");

        FlowProcessor.core().emit("rcv", "regist", e);


        e = WorkEventFactory.createOrigin().subscribe(event -> {
            logger.debug("XX - 1 " + " " + Thread.currentThread().getName() + " -- " + event.get("x"));
            TestUtil.waiting(1);
            //logger.debug("XX - 2 END");
        });
        e.setFireEventName("regist");
        e.put("id", "xx2");

        FlowProcessor.core().emit("rcv", "regist", e);


        TestUtil.waiting(3000);


    }


    private int countNo = 0;


    @Test
    public void testBasic3() {

        Re.flow(new PubSubBroker()).on("rcv").daemonMode().activate();



        Re.flow(e -> {


            WorkEvent newEvent = e;
            newEvent.put("no", countNo++);

            e.getActivity().getFlowProcessor().emit("rcv", newEvent);
            //logger.debug("FIRE " + e.hashCode());
            return Work.WAIT;
        }).interval(100,1000).activate();



        // subscribe

        Re.ceive("rcv", "regist", e -> {
            e.put("id", "demo");
        })
                .filter(event -> {
                    int val = (int) event.get("no");
                    if(val%3==0) {
                        return true;
                    }

                    return false;
                })
                .subscribe(event -> {
                    //int val = (Integer) event.get("no");
                    logger.debug("Re.ceive > " + event.get("no"));
                });

        TestUtil.waiting(3000);


        Re.ceive("rcv", "unregist", e -> {

            e.put("id", "demo");
        });


        TestUtil.waiting(500);


    }
}
