package isocline.reflow.module;

import isocline.reflow.FlowableWork;
import isocline.reflow.WorkEvent;
import isocline.reflow.WorkFlow;
import isocline.reflow.log.XLogger;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class PubSubBroker implements FlowableWork {


    private final XLogger logger = XLogger.getLogger(PubSubBroker.class);

    private final Map<String, WorkEvent> eventMap = new ConcurrentHashMap<>();

    private WorkEvent[] workEvents = null;

    private WorkEvent[] workEvents2 = null;

    private Collection<WorkEvent> collection;

    private Stream<WorkEvent> stream;

    private final int pararrelSize = 4;

    private int realPararrelSize = 0;

    private final int minUnitSize = 1;

    private boolean isNeedMakeArray = false;

    private final AtomicInteger seqCounter = new AtomicInteger();

    private int getPararrelSize() {
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

        //logger.info("init :: " + realPararrelSize + " "+workEvents2.length);


    }

    private void receive(WorkEvent event) {

        int seq = this.seqCounter.addAndGet(1);
        if (seq >= realPararrelSize) {
            return;
        }



        for (int i = (0 + seq); i < workEvents2.length; i = i + realPararrelSize) {
            try {
                workEvents2[i].callback(event.origin());
            } catch (Throwable e) {
                logger.error("========== "+i + " "+workEvents2.length);
                e.printStackTrace();
                String id = (String) workEvents2[i].get("callback_regist_id");
                eventMap.remove(id);
                isNeedMakeArray = true;

            }

        }




    }

    private void regist(WorkEvent event) {

        WorkEvent origin = event.origin();

        String id = (String) origin.get("id");

        origin.put("callback_regist_id", id);


        eventMap.put(id, origin);

        makeWorkEventArray();

        logger.debug("regist:" + id + " size= " + this.workEvents.length);

    }

    private synchronized void makeWorkEventArray() {

        isNeedMakeArray = false;
        this.collection = eventMap.values();
        this.workEvents = this.collection.toArray(new WorkEvent[collection.size()]);

    }

    private void unregist(WorkEvent event) {

        WorkEvent origin = event.origin();

        String id = (String) origin.get("id");

        eventMap.remove(id);

        makeWorkEventArray();

        logger.debug("unregist:" + id + " size= " + this.workEvents.length);
    }


    @Override
    public void defineWorkFlow(WorkFlow f) {
        f.next(this::receiveInit).runAsync(this::receive, this.getPararrelSize()).end();

        f.wait("regist").next(this::regist).end();
        f.wait("unregist").next(this::unregist).end();

    }
}
