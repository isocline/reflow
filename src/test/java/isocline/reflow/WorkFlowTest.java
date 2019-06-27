package isocline.reflow;

import isocline.reflow.flow.WorkFlowFactory;
import org.junit.Test;

public class WorkFlowTest extends TestBase {


    private String test(WorkEvent e) {
        System.out.println("zz");
        return "TEXT";
    }


    public int test1(String in) {
        System.out.println(in);
        return in.length();
    }

    private String test2(int in) {
        System.out.println(in);
        return in+"2";
    }

    @Test
    public void testMap() {

        WorkFlow<String> flow = WorkFlowFactory.createWorkFlow();

        flow.supply(this::test).pipe(this::test1).pipe(this::test2).end();


        Re.flow(flow).activate();

    }
}
