package isocline.reflow.examples.Re;

import isocline.reflow.*;
import isocline.reflow.event.WorkEventFactory;
import isocline.reflow.log.XLogger;
import isocline.reflow.module.WorkEventGenerator;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ReceiveTest {


    private XLogger logger = XLogger.getLogger(ReceiveTest.class);

    private Map<String, WorkEvent> eventMap = new HashMap<>();

    private WorkEvent[] workEvents = null;

    private WorkEvent[] workEvents2 = null;

    private Collection<WorkEvent> collection;

    private Stream<WorkEvent> stream;

    private int pararrelSize = 4;

    private int realPararrelSize = 0;

    private int minUnitSize = 1;

    private AtomicInteger seqCounter = new AtomicInteger();

    public int getPararrelSize() {
        return this.pararrelSize;
    }


    private void receiveInit(WorkEvent event) {
        if(workEvents.length==0) {
            realPararrelSize = 0;
            return;
        }


        workEvents2 = workEvents;
        seqCounter.set(-1);

        realPararrelSize =  (workEvents2.length-1)/minUnitSize+1;
        if(realPararrelSize>pararrelSize) {
            realPararrelSize = pararrelSize;
        }

        logger.info("real=="+realPararrelSize);



    }
    private void receive(WorkEvent event) {

        int seq = this.seqCounter.addAndGet(1);
        if(seq>=realPararrelSize) {
            return;
        }

        logger.debug("1 push "+eventMap.size() +" "+seq);

        for(int i=(0+seq);i<workEvents2.length;i=i+ realPararrelSize) {
            logger.debug("====> "+eventMap.size()  + " "+Thread.currentThread().getName() +" "+i);
            workEvents2[i].callback(event);
        }


        logger.debug("2 push "+eventMap.size());

    }

    private synchronized void regist(WorkEvent event) {

        WorkEvent origin = event.origin();

        String id = (String) origin.get("id");


        eventMap.put(id, origin);

        this.collection = eventMap.values();

        this.workEvents = this.collection.toArray(new WorkEvent[collection.size()]);

        logger.debug("regist:" + id + " size= "+this.workEvents.length);


    }

    private void unregist(WorkEvent event) {

    }


    @Test
    public void testBasic() {

        Re.flow(f -> {
            f.next(this::receiveInit).runAsync(this::receive,this.getPararrelSize()).end();
            //f.next(this::receive).end();

            f.wait("regist").next(this::regist).end();

        }).on("rcv").daemonMode().activate();

        WorkEventGenerator generator = new WorkEventGenerator();
        generator.setEventName("rcv");

        Re.task(generator).interval(500,1000).strictMode().activate();



        WorkEvent e = WorkEventFactory.createOrigin().subscribe(event -> {
            logger.debug("XX - 1 "+" "+Thread.currentThread().getName() );
            TestUtil.waiting(100);
            logger.debug("XX - 2");
        });
        e.setFireEventName("regist");
        e.put("id", "xx");

        FlowProcessor.core().emit("rcv", "regist", e);


        e = WorkEventFactory.createOrigin().subscribe(event -> {
            logger.debug("XX - 1 "+" "+Thread.currentThread().getName() );
            TestUtil.waiting(100);
            logger.debug("XX - 2");
        });
        e.setFireEventName("regist");
        e.put("id", "xx2");

        FlowProcessor.core().emit("rcv", "regist", e);


        TestUtil.waiting(3000);


    }
}
