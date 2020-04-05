package isocline.reflow.pattern;

import isocline.reflow.Time;
import isocline.reflow.WorkEvent;
import isocline.reflow.WorkFlow;
import isocline.reflow.WorkFlowPattern;

import java.util.HashMap;
import java.util.Map;

public class Bulkhead implements WorkFlowPattern {


    private String id;

    private int failCount;

    private long lastFailTime = 0;

    private int maxFailCount = 3;

    private long retryTimeGap = Time.SECOND * 10;

    private long timeout = 3000;

    private String timeoutEventName;


    private static Map<String, Bulkhead> map = new HashMap<>();

    public static Bulkhead create(String id) {

        return map.computeIfAbsent(id, Bulkhead::new);
    }


    private Bulkhead(String id) {
        this.id = id;
        this.timeoutEventName = "timeout-" + this.hashCode();
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

        System.err.println("!!!!! RAISE ERROR count== " + failCount + " " + e.getEventName());
    }

    private void ok(WorkEvent e) {
        failCount--;
        if (failCount < 0) {
        }
    }


    public boolean check(WorkEvent event) {

        System.err.println("FAIL COUNT: " + failCount + "  max :" + maxFailCount);

        long gap = System.currentTimeMillis() - lastFailTime;

        if (gap > retryTimeGap) {
            System.err.println("? === FAIL T");
            return true;
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


    @Override
    public void startFlow(WorkFlow flow) {

        flow.when(this::check);
    }


    @Override
    public void middleFlow(WorkFlow flow, int seq) {
    }

    @Override
    public void endFlow(WorkFlow flow) {

        String cursor = flow.cursor();

        flow.onError(cursor).accept(this::error);

        flow.wait(cursor).accept(this::ok);


    }


}
