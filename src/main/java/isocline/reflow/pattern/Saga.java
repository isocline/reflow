package isocline.reflow.pattern;

import isocline.reflow.WorkFlow;

import java.util.List;

public class Saga   {


    private WorkFlow workFlow;

    public static WorkFlow apply(WorkFlow workFlow, SagaWorkFlowFunc func) {


        SagaWorkFlow sfl = new SagaWorkFlow();

        func.apply(sfl);

        List<SagaWorkFlow.Basket> list = sfl.getList();

        for(SagaWorkFlow.Basket basket:list) {
            workFlow.next(basket.getTransaction());
        }



        return workFlow;

    }



}
