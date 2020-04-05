package isocline.reflow.examples.Re;

import isocline.reflow.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestTest {

    private Logger logger = LoggerFactory.getLogger(RequestTest.class);


    private long totalProcessTime = 200;

    private long methodCallCount = 5;


    public void testLoop(WorkEvent e) {
        logger.debug("testLoop");
        TestUtil.waiting(totalProcessTime / methodCallCount);

        //TestUtil.waiting(500);
        e.origin().put("price", Math.random());




        if( e.origin().dataChannel() != null) {


            Map map = (Map) e.origin().dataChannel().result();
           if(map!=null) {


               map.put("price", Math.random());
           }
        }
    }


    private AtomicInteger count = new AtomicInteger(0);
    int testCount = 200;

    @Test
    public void testRequest() throws Exception {

        FlowableWork flowableWork = f -> {

            int seq = 0;
            while (methodCallCount > seq++) {
                f.accept(this::testLoop);
            }

            f.end();
        };

        Re.flow(flowableWork)
                .on("chk")
                .daemonMode()
                .activate();

        Thread.sleep(1000);

        long t1 = System.currentTimeMillis();

        for (int i = 0; i < testCount; i++) {

            Re.quest("chk",
                    e -> {
                        e.put("ip", "192.168.0.1");
                    },
                    e -> {


                        double z = (double) e.get("price");
                        int c = count.addAndGet(1);
                        if (testCount <= c) {
                            long gap = System.currentTimeMillis() - t1;
                            logger.debug("process time:" + gap);
                        }
                        logger.debug(z + " " + c);
                    });
        }

        Thread.sleep(3000);

    }




    @Test
    public void testRequest2() throws Exception {

        FlowableWork flowableWork = f -> {

            int seq = 0;
            while (methodCallCount > seq++) {
                f.accept(this::testLoop);
            }

            f.end();
        };

        Re.flow(flowableWork)
                .on("lxq://local/biz/chk")
                .daemonMode()
                .activate();

        TestUtil.waiting(1000);




    }

    @Test
    public void testRequest3() throws Exception {
        System.err.println("zz");

        logger.info("cc");
        logger.debug("33");

        FlowableWork flowableWork = f -> {

            int seq = 0;
            /*
            while (methodCallCount > seq++) {
                f.apply(this::testLoop);
            }
            */
            f.accept(this::testLoop);

            f.end();
        };

        Re.flow(flowableWork)
                .on("lxq://")
                .daemonMode()
                .limitTps(15)
                .activate();

        TestUtil.waiting(1000);

        Map result = new HashMap<>();

        DataChannel dataChannel = new DataChannel("test");
        dataChannel.result(result);

        //Re.quest("lxq://local/biz/chk", dataChannel).block();

        System.out.println( result);


        for(int i=0;i<50;i++) {
            Re.quest("lxq://local/biz/chk", dataChannel , e->{
                logger.debug(">> "+e.getEventName());
                logger.debug(">> "+e.getThrowable());
                logger.debug(">> "+e.dataChannel().result());
            });
            TestUtil.waiting(100 - i*2);
        }


        TestUtil.waiting(500);

        for(int i=0;i<50;i++) {
            Re.quest("lxq://local/biz/chk", dataChannel , e->{
                logger.debug(">> "+e.getEventName());
                logger.debug(">> "+e.getThrowable());
                logger.debug(">> "+e.dataChannel().result());
            });
            TestUtil.waiting(100 - i*2);
        }

        logger.debug(">> >> "+result);









    }

    private void zzzz() {
        System.err.println("ENDDDD");

    }


    private void test1() {
        logger.debug("123");

    }



    private void push(WorkEvent e) {

        logger.debug("start push");


        Integer cnt = (Integer) e.get("count");
        if(cnt==null) {
            cnt = 0;
        }
        cnt++;
        e.put("count",cnt);

        if(cnt>1) {
            //e.getActivity().emit(WorkEventFactory.createOrigin("end"));
            e.propagate("end");
            System.err.println("END FIRE!!!!!!!~  "+cnt);
            return;
        }

       //


        Map map = (Map) e.origin().dataChannel().result();
        map.put("x", Math.random());
        map.put("s",cnt);

        logger.debug(cnt+ " send "+map);
        e.publish();
    }



    @Test
    public void testRequestPush() throws Exception {

        FlowableWork flowableWork = f -> {

            f.run(this::test1);



            f.flag("push").accept(this::push).fireEvent("push",2000);

            f.wait("end").run(this::zzzz).end();

        };




        Re.flow(flowableWork)
                .on("lxq://")
                .daemonMode()
                .activate();


        Map result = new HashMap<>();

        DataChannel dataChannel = new DataChannel("test");
        dataChannel.result(result);


        Re.quest("lxq://local/biz/chk", dataChannel ,e -> {
            //logger.debug(">>"+e.getEventName());
            System.err.println("0>>"+e.origin().dataChannel().result());
        });


        for(int i=0; i< 0;i++) {


            result = new HashMap<>();

            dataChannel = new DataChannel("test");
            dataChannel.result(result);

            Re.quest("lxq://local/biz/chk", dataChannel, e -> {
                //logger.debug(">>"+e.getEventName());
                logger.debug("1>>" + e.origin().dataChannel().result() +" "+Thread.activeCount());
            });
        }

        TestUtil.waiting(5000);

    }
}

