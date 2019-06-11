package isocline.reflow.event;

import isocline.reflow.WorkEvent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WorkEventTest {


    @Test
    public void testBasic() {

        WorkEvent event = WorkEventFactory.createOrigin("evt");

        assertEquals("evt", event.getEventName());

        assertEquals(null, event.get("key1"));

        event.remove("key1");


        event.put("key1", "val1");
        assertEquals("val1", event.get("key1"));

        event.put("key1", "val2");
        assertEquals("val2", event.get("key1"));

        event.remove("key1");
        assertEquals(null, event.get("key1"));

    }

    @Test
    public void testChildEvent() {

        WorkEvent event = WorkEventFactory.createOrigin("evt");

        assertEquals("evt", event.getEventName());

        event.remove("key1");
        event.put("key1", "val1");
        assertEquals("val1", event.get("key1"));


        WorkEvent event2 =event.createChild("evt2");
        assertEquals("evt2", event2.getEventName());

        assertEquals(null, event2.get("key1"));


        //event2.put("key1", "val2");
        assertEquals("val1", event2.origin().get("key1"));

    }
}
