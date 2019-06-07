package isocline.reflow.flow.func;

import isocline.reflow.WorkEvent;

@FunctionalInterface
public interface ReturnEventFunction {


    String checkFlow(WorkEvent event);
}
