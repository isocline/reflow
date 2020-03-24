package isocline.reflow.examples.basic;

import isocline.reflow.*;
import isocline.reflow.descriptor.CronDescriptor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledWork extends TestBase implements Work {

    private static Logger logger = LoggerFactory.getLogger(ScheduledWork.class);

    private int seq = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        logger.debug("activate:" + seq++);

        return WAIT;
    }


    @Test
    public void caseSchedule() throws Exception {

        String startTime = Time.toIsoDateFormat();
        String endTime = Time.toIsoDateFormat(System.currentTimeMillis() + 3000);
        logger.debug("startTime : " + startTime);
        logger.debug("endTime : " + endTime);

        Re.flow(new ScheduledWork())
                .interval(1 * Time.SECOND)
                .startTime(startTime)
                .finishTime("2020-06-16T16:00:00Z")
                .activate();

    }


    /**
     * execute with cron style
     *
     * @throws Exception
     */
    @Test
    public void caseCron() throws Exception {

        String endTime = Time.toIsoDateFormat(System.currentTimeMillis() + 3000);
        logger.debug("endTime : " + endTime); //"2020-06-16T16:00:00Z"

        Re.flow(new CronDescriptor("* 1,4-6 * * *"), new ScheduledWork())
                .finishTime(endTime)
                .activate().block();


    }


    @Test
    public void case2() throws Exception {


        Plan plan = Re.flow(ScheduledWork.class);

        plan.interval(1 * Time.SECOND)
                .finishTimeFromNow(3 * Time.SECOND)  // finish after 3 seconds
                .activate().block();


    }

    @Test
    public void caseStrictMode() throws Exception {

        Plan plan = Re.flow(ScheduledWork.class);

        plan.interval(1 * Time.SECOND)
                .finishTimeFromNow(3 * Time.SECOND)
                .strictMode()
                .activate().block();


    }

    @Test
    public void delayStart1() throws Exception {

        Plan plan = Re.flow(ScheduledWork.class);

        plan.interval(1 * Time.SECOND)
                .initialDelay(Time.milliseconds(0, 0, 2))
                .finishTimeFromNow(5 * Time.SECOND)
                .activate().block();


    }


    @Test
    public void delayStart2() throws Exception {

        Plan plan = Re.flow(ScheduledWork.class);

        plan.interval(1 * Time.SECOND)
                .startTime(Time.nextSecond() + Time.SECOND * 2)
                .finishTimeFromNow(5 * Time.SECOND)
                .activate().block();


    }


}
