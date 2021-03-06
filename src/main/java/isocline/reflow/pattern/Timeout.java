package isocline.reflow.pattern;

import isocline.reflow.*;

import java.util.HashMap;
import java.util.Map;

public class Timeout implements WorkFlowPattern {


    private final long timeout;

    private final String timeoutEventName;

    private static Map<String, Timeout> map = new HashMap<>();

    public static Timeout setup(long timeout) {

        return new Timeout(timeout);
    }


    private Timeout(long timeout) {
        this.timeout = timeout;
        this.timeoutEventName = "timeout-" + this.hashCode();
    }


    public void timeout(WorkEvent e) {
        e.getActivity().setError(new FlowProcessException("timeout"));
    }



    public void startFlow(WorkFlow flow) {

        flow.fireEvent("error::" + timeoutEventName, this.timeout);
    }


    public void middleFlow(WorkFlow flow, int seq) {
    }


    public void endFlow(WorkFlow flow) {

        flow.onError(this.timeoutEventName).accept(this::timeout).end();
    }


}
