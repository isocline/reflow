package isocline.reflow;

import org.junit.AfterClass;

public class TestBase {

    @AfterClass
    public static void shutdown() {
        FlowProcessor.core().shutdown(3000);
    }


}