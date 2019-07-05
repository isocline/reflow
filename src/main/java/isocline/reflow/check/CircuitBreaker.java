package isocline.reflow.check;

import isocline.reflow.Time;
import isocline.reflow.WorkEvent;

import java.util.HashMap;
import java.util.Map;

public class CircuitBreaker {

    private final String id;
    private int failCount;

    private long lastFailTime = 0;

    private int maxFailCount = 3;

    private final long retryTimeGap = Time.SECOND * 10;


    private static final Map<String, CircuitBreaker> map = new HashMap<>();

    public static CircuitBreaker create(String id) {

        return map.computeIfAbsent(id, CircuitBreaker::new);
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
            event.getActivity().setError(new RuntimeException("circuit open"));
            System.err.println("? === FAIL 1");
            return false;
        }

    }

}
