package isocline.reflow.flow.func;

import isocline.reflow.WorkEvent;

@FunctionalInterface
public interface CheckFunction {


    boolean check(WorkEvent event);
}
