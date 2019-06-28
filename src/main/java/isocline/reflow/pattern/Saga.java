package isocline.reflow.pattern;

import isocline.reflow.WorkEvent;
import isocline.reflow.WorkFlow;
import isocline.reflow.flow.func.WorkEventConsumer;

import java.util.List;
import java.util.function.Consumer;

public class Saga   {


    private static void error(WorkEvent e) {

        e.getThrowable().printStackTrace();

    }

    public static WorkFlow apply(WorkFlow workFlow, Consumer<SagaWorkFlow> func) {


        SagaWorkFlow sfl = new SagaWorkFlow();

        func.accept(sfl);

        List<SagaWorkFlow.Basket> list = sfl.getList();

        for(SagaWorkFlow.Basket basket:list) {

            WorkEventConsumer t = basket.getTransaction();

            workFlow.next(t, basket.getEventName());
        }

        workFlow.end();


        int size = list.size();

        for(int j=(size-1);j>=0;j--) {
            SagaWorkFlow.Basket basket = list.get(j);
            String eventName = basket.getEventName();

            WorkFlow f = workFlow.onError(eventName);

            for(int i=j;i>=0;i--) {
                f=f.next(list.get(i).getConpensation());
            }
            f.end();
        }

        workFlow.onError("*").next(Saga::error);

        return workFlow;

    }



}
