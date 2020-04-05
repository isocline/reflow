package isocline.reflow.pattern;

import isocline.reflow.WorkEvent;
import isocline.reflow.WorkFlow;
import isocline.reflow.flow.func.WorkEventConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


/**
 * Saga interaction pattern (Long-running transaction) [https://en.wikipedia.org/wiki/Long-running_transaction]
 */
public class Saga {

    private boolean printError = true;

    private static void error(WorkEvent e) {

        e.getThrowable().printStackTrace();

    }



    public static Saga init() {
        return init(null);
    }


    public static Saga init(Consumer<Saga> config) {
        Saga saga = new Saga();

        if (config != null) {
            config.accept(saga);
        }



        return saga;
    }

    protected void setPrintError(boolean isPrintError) {
        this.printError = isPrintError;
    }


    public WorkFlow apply(WorkFlow workFlow, Consumer<SagaFlow> func) {


        SagaFlow sfl = new SagaFlow();

        func.accept(sfl);

        List<Basket> list = sfl.getList();

        for (Basket basket : list) {

            WorkEventConsumer t = basket.getTransaction();

            workFlow.accept(t, basket.getEventName());
        }

        workFlow.end();


        int size = list.size();

        for (int j = (size - 1); j >= 0; j--) {
            Basket basket = list.get(j);
            String eventName = basket.getEventName();

            WorkFlow f = workFlow.onError(eventName);

            for (int i = j; i >= 0; i--) {
                f = f.accept(list.get(i).getCompensation());
            }
            f.end();
        }

        if(printError) {
            workFlow.onError("*").accept(Saga::error);
        }



        return workFlow;

    }


    protected static class SagaFlow {

        private final List<Basket> list = new ArrayList<>();


        protected SagaFlow transaction(WorkEventConsumer runnable, WorkEventConsumer compensation) {

            list.add(new Basket(runnable, compensation));

            return this;
        }

        private List<Basket> getList() {
            return this.list;
        }


    }

    private static class Basket {
        private final WorkEventConsumer t;
        private final WorkEventConsumer c;

        private final String eventName;

        Basket(WorkEventConsumer t, WorkEventConsumer c) {
            this.t = t;
            this.c = c;

            this.eventName = "ST-" + t.hashCode();
        }

        WorkEventConsumer getTransaction() {
            return this.t;
        }

        WorkEventConsumer getCompensation() {
            return c;
        }

        String getEventName() {
            return this.eventName;
        }
    }


}
