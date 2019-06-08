package isocline.reflow.dummy;

import isocline.reflow.WorkEvent;
import isocline.reflow.FlowProcessor;
import org.junit.Test;

import java.util.List;

import static isocline.reflow.WorkHelper.GetResultList;
import static isocline.reflow.WorkHelper.Return;

/**
 * @author jibumjung
 */
public class CompletableFutureTest3 {

    // 두개의 비동기 요청을 동시에 진행해서 조합 할 수 있다.
    @Test
    public void thenCombineTest() throws Exception {
        Price price = new Price();





        FlowProcessor.main().reflow(flow -> {
            flow.runAsync(e -> {
                Return(e, price.calculatePrice(e.count()));
            },5).waitAll().next( (WorkEvent e) -> {
                List<Double> list = GetResultList(e);
                double result = list.stream().mapToDouble(i -> i).sum();

                System.out.println("result=" + result);
            });
        }).activate().block();


        FlowProcessor.main().shutdown();

    }

    static class Price {
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