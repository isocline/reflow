package isocline.reflow.examples.crontab;

import isocline.reflow.*;
import isocline.reflow.descriptor.CronDescriptor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrontSchedule implements Work {

    private static Logger logger = LoggerFactory.getLogger(CrontSchedule.class);

    private int count = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        logger.debug("activate:" + count++);


        if(count==2) return TERMINATE;

        return WAIT;
    }

    @Test
    public void case1() throws Exception {


        FlowProcessor.core()
                .task(CrontSchedule.class)
                .describe(new CronDescriptor("* * * * *"))
                .activate();

        FlowProcessor.core().shutdown(10* Time.SECOND);


    }

    @Test
    public void case2() throws Exception {

        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.task(new CronDescriptor("* * * * *"), CrontSchedule.class);

        schedule.activate();

        processor.shutdown(Time.MINUTE);


    }


}
