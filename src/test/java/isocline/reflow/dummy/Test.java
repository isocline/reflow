package isocline.reflow.dummy;

import isocline.reflow.*;
import isocline.reflow.log.XLogger;


public class Test {

    public static void main(String[] args) throws Exception {


        FlowProcessor processor = FlowProcessorFactory.getProcessor();




        for (int i = 0; i < 1; i++) {

            TestJob work = new TestJob(i);
            //Plan schedule = processor.Reflow(work).on("fire").setStartDelay(1000);
            //Plan schedule = processor.Reflow(work).on("fire").setStartDelay(Clock.milliseconds("2019-01-17T13:32:30+09:00"));
            Plan schedule = processor.reflow(work).on("fire").strictMode();
            //Plan schedule = processor.Reflow(work).on("fire");

            schedule.activate();

        }

        /*
        for(int i=0;i<30;i++) {

            if(worker.getManagedWorkCount()==0) {
                break;
            }

            System.out.println("WORKER SIZE = "+ worker.getWorkQueueSize() +"  activate");
            System.out.println("WORK COUNT = "+worker.getManagedWorkCount() +"  chk");

            if(i==15) {
                WorkEvent event = new WorkEvent();
                event.put("x","X value setup");
                worker.emit("fire", event );
                System.err.println("xxxxxxx");
            }

            Thread.sleep(3000);
        }
        */


        processor.shutdown(10000);
    }


    public static class TestJob implements Work {

        protected static XLogger logger = XLogger.getLogger(TestJob.class);


        private int seq;
        private int count = 0;

        public TestJob(int seq) {
            this.seq = seq;
        }

        public long execute(WorkEvent event) throws InterruptedException {

            count ++;

            Object eventMsg = event.get("x");
            log(seq + "th job activate. count="+count + " "+eventMsg);

            if(eventMsg!=null) {
                return TERMINATE;
            }



            if (count <= 3) {
                return Clock.SECOND;
            } else if (count >3 && count<6) {
                return 2*Clock.SECOND;
            } else if(count >= 6) {
                return WAIT;
            }else {
                return TERMINATE;
            }


        }

        private void log(String msg) {

            logger.debug(msg);

        }
    }


}
