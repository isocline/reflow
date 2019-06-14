package isocline.reflow.pattern;

import isocline.reflow.Clock;
import isocline.reflow.WorkEvent;
import isocline.reflow.WorkFlow;
import isocline.reflow.WorkFlowPattern;

import java.util.HashMap;
import java.util.Map;

public class CircuitBreaker implements WorkFlowPattern {



    private String id;
    private int failCount;

    private long lastFailTime = 0;

    private int maxFailCount = 3;

    private long retryTimeGap = Clock.SECOND * 10;

    private long timeout = 3000;

    private String timeoutEventName;


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
        this.timeoutEventName = "timeout-"+this.hashCode();
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

        System.err.println("!!!!! RAISE ERROR count== "+failCount + " "+e.getEventName()) ;
    }

    private void ok(WorkEvent e) {
        failCount--;
        if(failCount<0) {
        }
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


    @Override
    public void startFlow(WorkFlow flow) {
        System.err.println("1 ---"+this.timeoutEventName);
        flow.fireEvent("error::"+timeoutEventName, this.timeout)
                .check(this::check);
    }

    @Override
    public void middleFlow(WorkFlow flow, int seq) {
        return;
    }

    @Override
    public void endFlow(WorkFlow flow) {
        System.err.println("2 ---"+this.timeoutEventName);
        String cursor = flow.cursor();

        flow.next(this::ok);

        //flow.onError(cursor, this.timeoutEventName).next(this::error).inactive();
        flow.onError("*").next(this::error).end();
    }



}
