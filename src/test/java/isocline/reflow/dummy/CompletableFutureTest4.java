package isocline.reflow.dummy;

import isocline.reflow.FlowProcessor;
import isocline.reflow.FlowableWork;
import isocline.reflow.WorkEvent;
import org.junit.Test;

/**
 * @author
 */
public class CompletableFutureTest4 {


    @Test
    public void thenCombineTest() throws Exception {
        Price price = new Price();

        System.out.println("---1");

        FlowableWork flow1 = (flow) -> {
            flow.mapAsync(e -> price.calculatePrice(1),
                    e -> price.calculatePrice(2))
                    .next((WorkEvent e) -> e.getDoubleStream().sum());
        };


        FlowProcessor.core()
                .reflow(flow1)
                //.daemon()
                .activate(System.out::println);

        System.out.println("---2");


        FlowProcessor.core().awaitShutdown();

    }

    static class Price {
        public void print() {
            System.out.println("START");
        }

        public double getPrice(double oldprice) throws Exception {
            return calculatePrice(oldprice);
        }

        public double calculatePrice(double oldprice) throws Exception {
            System.out.println("Input :" + oldprice);
            Thread.sleep(1000l);
            System.out.println("Output :" + (oldprice + 1l));
            return oldprice + 1l;
        }


    }
}