package isocline.reflow.pattern;

import isocline.reflow.FlowPattern;
import isocline.reflow.WorkEvent;
import isocline.reflow.WorkFlow;
import isocline.reflow.flow.func.WorkEventConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Saga implements FlowPattern {


    public static Saga instance() {
        return new Saga();
    }


    private static void error(WorkEvent e) {

        e.getThrowable().printStackTrace();

    }

    private WorkFlow workFlow;

    public static Saga init(WorkFlow flow) {
        return init(flow, null);
    }


    public static Saga init(WorkFlow flow, Consumer<Saga> config) {
        Saga saga = new Saga();

        if(config!=null) {
            config.accept(saga);
        }

        saga.workFlow = flow;

        return saga;
    }

    public WorkFlow apply(  Consumer<SagaWorkFlow> func) {


        SagaWorkFlow sfl = new SagaWorkFlow();

        func.accept(sfl);

        List<SagaWorkFlow.Basket> list = sfl.getList();

        for (SagaWorkFlow.Basket basket : list) {

            WorkEventConsumer t = basket.getTransaction();

            workFlow.next(t, basket.getEventName());
        }

        workFlow.end();


        int size = list.size();

        for (int j = (size - 1); j >= 0; j--) {
            SagaWorkFlow.Basket basket = list.get(j);
            String eventName = basket.getEventName();

            WorkFlow f = workFlow.onError(eventName);

            for (int i = j; i >= 0; i--) {
                f = f.next(list.get(i).getConpensation());
            }
            f.end();
        }

        workFlow.onError("*").next(Saga::error);

        return workFlow;

    }




    @Override
    public void end(WorkFlow workFlow) {


        for (Basket basket : list) {

            WorkEventConsumer t = basket.getTransaction();

            workFlow.next(t, basket.getEventName());
        }

        workFlow.end();


        int size = list.size();

        for (int j = (size - 1); j >= 0; j--) {
            Basket basket = list.get(j);
            String eventName = basket.getEventName();

            WorkFlow f = workFlow.onError(eventName);

            for (int i = j; i >= 0; i--) {
                f = f.next(list.get(i).getConpensation());
            }
            f.end();
        }

        workFlow.onError("*").next(Saga::error);


    }


    private List<Basket> list = new ArrayList<>();


    public Saga transaction(WorkEventConsumer runnable, WorkEventConsumer compentation) {

        list.add(new Basket(runnable, compentation));

        return this;
    }

    private List<Basket> getList() {
        return list;
    }


    private class Basket {
        private WorkEventConsumer t;
        private WorkEventConsumer c;

        private String eventName;

        Basket(WorkEventConsumer t, WorkEventConsumer c) {
            this.t = t;
            this.c = c;

            this.eventName = "ST-" + t.hashCode();
        }

        public WorkEventConsumer getTransaction() {
            return this.t;
        }

        public WorkEventConsumer getConpensation() {
            return c;
        }

        public String getEventName() {
            return this.eventName;
        }
    }
}
