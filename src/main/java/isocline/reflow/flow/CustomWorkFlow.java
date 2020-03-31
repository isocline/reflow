package isocline.reflow.flow;

import isocline.reflow.WorkFlow;

public abstract class CustomWorkFlow<T> extends WorkFlowWrapper<T> {

    protected CustomWorkFlow() {

    }

    protected CustomWorkFlow(WorkFlow workFlow) {
        super(workFlow);

    }


    public abstract void closePattern();




}
