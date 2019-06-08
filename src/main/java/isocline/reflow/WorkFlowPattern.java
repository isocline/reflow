package isocline.reflow;

public interface WorkFlowPattern {


    void startFlow(WorkFlow flow);


    void middleFlow(WorkFlow flow, int seq);



    void endFlow(WorkFlow flow);
}
