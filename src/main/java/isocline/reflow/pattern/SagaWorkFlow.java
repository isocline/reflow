package isocline.reflow.pattern;

import isocline.reflow.flow.func.WorkEventConsumer;

import java.util.ArrayList;
import java.util.List;

public class SagaWorkFlow {

    private List<Basket> list = new ArrayList<>();


    public SagaWorkFlow transaction(WorkEventConsumer runnable, WorkEventConsumer compentation) {

        list.add(new Basket(runnable, compentation));

        return this;
    }

    public List<Basket> getList() {
        return this.list;
    }


    public class Basket {
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
