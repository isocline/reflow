package isocline.reflow;

import isocline.reflow.log.XLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ConfigurationTest {

    private static XLogger logger = XLogger.getLogger(ConfigurationTest.class);



    @Before
    public void before() {

    }

    @After
    public void after() {


    }

    @Test
    public void testBasic() throws Exception {

        int size = 2;

        Configuration conf = Configuration.create().setInitThreadWorkerSize(size);

        assertEquals(size, conf.getInitThreadWorkerSize());

        size=10;
        conf = Configuration.create().setMaxThreadWorkerSize(size);

        assertEquals(size, conf.getMaxThreadWorkerSize());




        conf = Configuration.create().setThreadPriority(Thread.NORM_PRIORITY);

        assertEquals(Thread.NORM_PRIORITY, conf.getThreadPriority());


        conf = Configuration.create().setExecuteTimeout(1000);

        assertEquals(1000, conf.getExecuteTimeout());

    }


    @Test
    public void testLocking() throws Exception {



        Configuration conf = Configuration.create().setInitThreadWorkerSize(1).lock();

        try {
            conf.setInitThreadWorkerSize(3);
            assertEquals(true, false);
        }catch(RuntimeException ignored) {

        }



    }

}
