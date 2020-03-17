package isocline.reflow;

import org.junit.AfterClass;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TestBase {

    private Map<String, AtomicInteger> counterMap = new Hashtable();

    protected static long shutdownTimeout = 3000;


    @AfterClass
    public static void shutdown() {
        if(shutdownTimeout>0) {
            FlowProcessor.core().shutdown(shutdownTimeout);
            shutdownTimeout = 3000;
        }

    }


    public AtomicInteger getCounter(String key) {
        AtomicInteger counter = counterMap.computeIfAbsent(key, k -> new AtomicInteger(0));

        return counter;
    }


}