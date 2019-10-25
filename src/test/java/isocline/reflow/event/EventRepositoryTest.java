package isocline.reflow.event;

import isocline.reflow.WorkEvent;
import isocline.reflow.flow.FunctionExecutor;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EventRepositoryTest {

    EventRepository<String, Queue<FunctionExecutor>> eventRepository = new EventRepository();

    @Test
    public void testBasic() {

        String eventName = "event1";

        String[] subEventNames = eventRepository.setBindEventNames(eventName);

        assertEquals(1, subEventNames.length);
        assertEquals(eventName, subEventNames[0]);

    }

    @Test
    public void testMultiEvents() {

        String eventName = "event1&event2";

        String[] subEventNames = eventRepository.setBindEventNames(eventName);

        assertEquals(2, subEventNames.length);
        assertEquals("event1", subEventNames[0]);
        assertEquals("event2", subEventNames[1]);


        WorkEvent e = WorkEventFactory.createOrigin();

        SimultaneousEventSet simultaneousEventSet = eventRepository.getSimultaneousEventSet("event1");

        if (simultaneousEventSet == null || simultaneousEventSet.isRaiseEventReady(e,"event1")) {
            fail();
        }

        simultaneousEventSet = eventRepository.getSimultaneousEventSet("event3");

        if (simultaneousEventSet == null || simultaneousEventSet.isRaiseEventReady(e,"event3")) {

        }else {
            fail();
        }

        simultaneousEventSet = eventRepository.getSimultaneousEventSet("event2");

        if (simultaneousEventSet == null || simultaneousEventSet.isRaiseEventReady(e,"event2")) {

        }else{
            fail();
        }
    }


    private void bindEventRepository(String eventName, FunctionExecutor functionExecutor) {


        Queue<FunctionExecutor> queue = this.eventRepository.computeIfAbsent(eventName, k -> new ConcurrentLinkedQueue<>());

        queue.add(functionExecutor);

    }
}
