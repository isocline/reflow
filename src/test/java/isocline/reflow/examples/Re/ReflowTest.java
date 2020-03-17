package isocline.reflow.examples.Re;

import isocline.reflow.*;
import isocline.reflow.log.XLogger;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflowTest {

    private XLogger logger = XLogger.getLogger(ReceiveTest.class);

    private void test1() {
        logger.debug("test 1");
    }

    private void test2() {
        logger.debug("test 2");

        Map input = new HashMap();
        Map result = new HashMap<>();

        input.put("key","val");
        input.put("kdf", "sdf");

        DataChannel dataChannel = new DataChannel(input);
        dataChannel.result(result);


        Re.quest("lxq://local/biz/chk", dataChannel ,e -> {
            System.err.println("0>>"+e.origin().dataChannel().result());
        });


    }


    private void logging(WorkEvent e) {
        logger.debug("log s");
        try {
            Thread.sleep(1000);
        }catch (Exception ee) {

        }
        logger.debug("log e");
    }
    private void checkConnection(WorkEvent e) {

        logger.debug("checkConnection");
    }

    private void receive(WorkEvent e) {
        logger.debug("test3");
        DataChannel dataChannel = e.origin().dataChannel();

        Map inputStr = (Map ) dataChannel.source();

        Map map = (Map) dataChannel.result();
        map.put("size", inputStr.size());
        map.put("z",Math.random());
    }

    @Test
    public void testFlow() {

        WorkFlow flow = WorkFlow.create();
        flow.next(this::test1).next(this::test2);
        flow.end();

        Re.flow(flow).activate();

    }


    @Test
    public void testRepeat() {

        WorkFlow flow = WorkFlow.create();
        flow.runAsync(this::logging);
        flow.runAsync(this::checkConnection).next(this::receive);
        flow.runAsync(this::logging);
        flow.end();

        Re.flow(flow)
                .on("lxq://")
                .daemonMode()
                .activate();


        Re.play(e->{

            testFlow();

            return Work.WAIT;
        }).activate().block();

    }

    private String to(String in) {
        return in+"xx";
    }

    @Test
    public void testStream() {
        String[] test = new String[] {"abc","b123","c123123"};
        Stream<String> stream = Arrays.stream(test);

        String result = stream.filter(s-> s.length()>3).map(s->s+"__").map(this::to).collect(Collectors.joining());
        System.err.println(result);
    }
}
