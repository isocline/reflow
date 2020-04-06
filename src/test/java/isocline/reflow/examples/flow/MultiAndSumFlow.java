package isocline.reflow.examples.flow;

import isocline.reflow.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MultiAndSumFlow implements FlowableWork {


    private static Logger logger = LoggerFactory.getLogger(MultiAndSumFlow.class);

    public void async1() {
        logger.debug("** invoke - async1");
        TestUtil.waiting(500);
        logger.debug("** invoke - async1 - END");
    }

    public void async2() {
        logger.debug("** invoke - async2");
        TestUtil.waiting(600);
        logger.debug("** invoke - async2 - END");
    }


    public void sum1() {
        logger.debug("** invoke - sum1");
        TestUtil.waiting(300);
        logger.debug("** invoke - sum1 - END");
    }

    public String async3(String x) {
        logger.debug("** invoke - async3");
        TestUtil.waiting(500);

        logger.debug("** invoke - async1 - END");

        return x +" zz";
    }

    public String async4(String y) {
        logger.debug("** invoke - async4");
        TestUtil.waiting(600);

        logger.debug("** invoke - async2 - END");

        return y+ " tt";
    }


    public String sum2(String x,String y) {
        logger.debug("** invoke - sum2" );
        TestUtil.waiting(300);
        logger.debug("** invoke - sum2 - END");

        return x+y ;
    }



    public void defineWorkFlow(WorkFlow flow) {


        flow.runAsync(this::async1,"h1").runAsync(this::async2,"h2");

        flow.waitAll("h1","h2").run(this::sum1);

        //flow.runAsync(this::async3,"h3").runAsync(this::async4,"h4");

        //flow.waitAll("h3","h4").apply(this::sum2).end();

    }


    @Test
    public void test() throws InterruptedException {

        Activity activity  = start(false);

        activity.block();

    }

    private String zzz;


    public void test2(final String x, final String y) throws Exception {


        WorkFlow wf = WorkFlow.create();

        wf.runAsync(e -> {
            String result = async3(x);
            e.put("resultX",result);

        },"h3").runAsync(e -> {
            String result = async4(y);
            e.put("resultY",result);
        },"h4");

        wf.waitAll("h3","h4").accept(e->{
            String result = sum2(e.get("resultX").toString(), e.get("resultY").toString() );

            zzz=result;

            WorkHelper.Return(e,result);


        }).end();


        WorkEvent e= Re.flow(wf).activate(result->{
            System.err.println(result+"<<");

        }).block().getWorkEvent();


        System.err.println(WorkHelper.Get(e)+"  <<");




    }



    @Test
    public void test4() throws Exception{
        test2("z","y");
    }


}
