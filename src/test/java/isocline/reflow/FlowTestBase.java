package isocline.reflow;

import org.junit.AfterClass;

public class FlowTestBase {

    @AfterClass
    public void shutdown() {
        FlowProcessor.core().shutdown(3000);
    }
}