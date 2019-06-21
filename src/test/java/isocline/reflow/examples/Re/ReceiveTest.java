package isocline.reflow.examples.Re;

import isocline.reflow.FlowProcessor;
import isocline.reflow.Re;
import isocline.reflow.TestUtil;
import isocline.reflow.WorkEvent;
import isocline.reflow.event.WorkEventFactory;
import isocline.reflow.log.XLogger;
import isocline.reflow.module.WorkEventGenerator;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ReceiveTest {


    private XLogger logger = XLogger.getLogger(ReceiveTest.class);

    private Map<String, WorkEvent> eventMap = new HashMap<>();

    private Collection<WorkEvent> collection;


    private void receive(WorkEvent event) {

        logger.debug("push "+eventMap.size());

        this.collection.stream().forEach(event1 -> event1.callback(event));

    }

    private void regist(WorkEvent event) {

        WorkEvent origin = event.origin();

        String id = (String) origin.get("id");

        logger.debug("regist:" + id);

        eventMap.put(id, origin);

        this.collection = eventMap.values();


    }

    private void unregist(WorkEvent event) {

    }


    @Test
    public void testBasic() {

        Re.flow(f -> {
            f.next(this::receive).end();

            f.wait("regist").next(this::regist).end();

        }).on("rcv").daemonMode().activate();

        WorkEventGenerator generator = new WorkEventGenerator();
        generator.setEventName("rcv");

        Re.task(generator).interval(500,150).strictMode().activate();



        WorkEvent e = WorkEventFactory.createOrigin().subscribe(event -> {
            logger.debug("XX - " + event);
        });
        e.setFireEventName("regist");
        e.put("id", "xx");

        FlowProcessor.core().emit("rcv", "regist", e);


        TestUtil.waiting(1000000);


    }
}
