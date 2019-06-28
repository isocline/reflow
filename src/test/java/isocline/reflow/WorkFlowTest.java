package isocline.reflow;

import isocline.reflow.flow.WorkFlowFactory;
import org.junit.Assert;
import org.junit.Test;

public class WorkFlowTest extends TestBase {


    @Test
    public void textExtract() {

        TestService svc = new TestService();


        Re.flow(f -> {
            f.extract(svc::extractor).end();
        }).activate((String result) -> {
            Assert.assertEquals("TEXT", result);
        });

    }

    @Test
    public void textTrans() {
        TestService svc = new TestService();

        WorkFlow<Void> flow = WorkFlowFactory.create();

        flow.extract(svc::extractor).trans(svc::test1).trans(svc::test2).end();

        Re.flow(flow).activate(svc::result);

    }

    @Test
    public void textRunAsync() {
        TestService svc = new TestService();

        WorkFlow<Void> flow = WorkFlowFactory.create();

        flow.runAsync(svc::exec1);
        flow.runAsync(svc::exec2);

        flow.waitAll().end();

        Re.flow(flow).activate();

        TestUtil.waiting(10);

        Assert.assertEquals(2, svc.count.get());

        TestUtil.waiting(50);

        Assert.assertEquals(0, svc.count.get());

    }

    @Test
    public void textRunAsyncMulti() {
        TestService svc = new TestService();

        WorkFlow<Void> flow = WorkFlowFactory.create();

        flow.runAsync(svc::exec1, 5);


        flow.waitAll().end();

        Re.flow(flow).activate();

        TestUtil.waiting(10);

        Assert.assertEquals(5, svc.count.get());

        TestUtil.waiting(50);

        Assert.assertEquals(0, svc.count.get());

    }
}
