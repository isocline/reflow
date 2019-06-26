package isocline.reflow.dummy;

import isocline.reflow.*;
import org.junit.Test;

public class Test3 {

    @Test
    public void test1() throws Exception {

        // make plan from call
        Plan plan = Re.call(e -> {
            System.out.println(e.count());
        });


        Activity activity = plan
                .interval(500, 10 * Time.SECOND)
                .activate();


        Re.call(e -> {
            System.out.println(e.count());
        })
                .interval(123, 1 * Time.SECOND)
                .activate();

        FlowProcessor.core().shutdown(3000);

    }

    /*
    @Test
    public void test2() throws Exception {

        FlowableWork call = f->{
          f.next(System.out::println)
        };

        Re.call((WorkEvent e)->{
            System.out.println(e.count()*2);
            return 1*Time.SECOND;
        })
                .initialDelay(Time.SECOND)
                .activate().block();

    }
    */
}
