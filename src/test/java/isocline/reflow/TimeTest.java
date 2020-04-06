package isocline.reflow;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.junit.Assert.assertEquals;


public class TimeTest {

    private static Logger logger = LoggerFactory.getLogger(TimeTest.class);


    @Before
    public void before() {

    }

    @After
    public void after() {

    }

    @Test
    public void testTime() throws Exception {

        long p = 2 * Time.HOUR + 3 * Time.MINUTE + 7 * Time.SECOND;

        long q = Time.milliseconds(2, 3, 7);

        assertEquals(p, q);

    }


    @Test
    public void testISOTime() throws Exception {

        String isoTime = "2029-05-14T11:59:59+09:00";
        Date d = Time.toDate(isoTime);


        long p = Time.milliseconds(isoTime);


        logger.debug("time {}" ,d);
        logger.debug("time {}" ,p);

        assertEquals(true, p > 0);

    }


    @Test
    public void nextSecond() throws Exception {

        System.out.println(Time.toDateFormat(System.currentTimeMillis()));

        long t = Time.nextSecond();

        logger.debug(Time.toDateFormat(t));

    }


    @Test
    public void nextMinutes() throws Exception {

        String t1 = Time.toDateFormat(System.currentTimeMillis());

        logger.debug(t1);

        long t = Time.nextMinutes();

        String t2 = Time.toDateFormat(t);

        logger.debug(t2);

        String[] items1 = t1.split(":");
        String[] items2 = t2.split(":");

        int it1 = Integer.parseInt(items1[1]);
        int it2 = Integer.parseInt(items2[1]);

        // if it1 is 59,  it2 is 0.
        if(it2!=0) {
            assertEquals(it1 + 1, it2);
        }



        float f1 = Float.parseFloat(items2[2]);


        assertEquals(0.0, f1, 0);

    }


}
