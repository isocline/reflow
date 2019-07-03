package isocline.reflow;

import org.junit.AfterClass;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TestBase {

    private Map<String, AtomicInteger> counterMap = new Hashtable();

    @AfterClass
    public static void shutdown() {
        FlowProcessor.core().shutdown(3000);
    }


    public AtomicInteger getCounter(String key) {
        AtomicInteger counter = counterMap.computeIfAbsent(key, k -> new AtomicInteger(0));

        return counter;
    }


}