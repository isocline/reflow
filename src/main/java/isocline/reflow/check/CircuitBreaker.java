package isocline.reflow.check;

import isocline.reflow.Clock;
import isocline.reflow.WorkEvent;

import java.util.HashMap;
import java.util.Map;

public class CircuitBreaker {

    private String id;
    private int failCount;

    private long lastFailTime = 0;

    private int maxFailCount = 3;

    private long retryTimeGap = Clock.SECOND * 10;


    private static Map<String, CircuitBreaker> map = new HashMap<>();

    public static CircuitBreaker create(String id) {

        CircuitBreaker circuitBreaker = map.get(id);

        if (circuitBreaker == null) {
            circuitBreaker = new CircuitBreaker(id);
            map.put(id, circuitBreaker);
        }

        return circuitBreaker;
    }


    private CircuitBreaker(String id) {
        this.id = id;
    }

    public void setMaxFailCount(int maxFailCount) {
        this.maxFailCount = maxFailCount;
    }

    public void timeout(WorkEvent e) {
        failCount++;
        lastFailTime = System.currentTimeMillis();
    }

    public void error(WorkEvent e) {


        failCount++;
        lastFailTime = System.currentTimeMillis();

        System.err.println("!!!!! RAISE ERROR count== "+failCount);
    }



    public boolean check(WorkEvent event) {

        System.err.println( "FAIL COUNT: "+failCount + "  max :"+maxFailCount);

        long gap = System.currentTimeMillis() - lastFailTime;

        if (gap > retryTimeGap) {
            System.err.println("? === FAIL T");
            return  true;
        }

        if (maxFailCount > failCount) {
            System.err.println("___ OK");
            return true;
        } else {
            event.getPlan().setError(new RuntimeException("circuit open"));
            System.err.println("? === FAIL 1");
            return false;
        }

    }

}
