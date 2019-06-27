package isocline.reflow.pattern;

import isocline.reflow.flow.func.ThrowableRunFunction;

import java.util.ArrayList;
import java.util.List;

public class SagaWorkFlow {

    private List<Basket> list = new ArrayList<>();


    public SagaWorkFlow transaction(ThrowableRunFunction runnable, ThrowableRunFunction compentation) {

        list.add(new Basket(runnable, compentation));

        return this;
    }

    public List<Basket> getList() {
        return this.list;
    }


    public  class Basket {
        private ThrowableRunFunction t;
        private ThrowableRunFunction c;

        Basket(ThrowableRunFunction t, ThrowableRunFunction c) {
            this.t = t;
            this.c = c;
        }

       public  ThrowableRunFunction getTransaction() {
            return this.t;
        }

        public ThrowableRunFunction getConpensation() {
            return c;
        }
    }
}
