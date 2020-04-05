package isocline.reflow.pattern;

import isocline.reflow.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class CircuitBreaker implements WorkFlowPattern {


    private static Map<String, CircuitBreaker.Config> configMap = new HashMap<>();


    private CircuitBreaker.Config config;

    private CircuitBreaker() {

    }


    @Override
    public void startFlow(WorkFlow flow) {
        flow.fireEvent("error::" + config.timeoutEventName, config.timeout)
                .when(config::check);
    }

    @Override
    public void middleFlow(WorkFlow flow, int seq) {
    }

    @Override
    public void endFlow(WorkFlow flow) {
        String cursor = flow.cursor();

        flow.accept(config::ok);

        //flow.onError(cursor, this.timeoutEventName).apply(this::error).inactive();
        flow.onError("*").accept(config::error).end();
    }

    private WorkFlow flow;


    public static CircuitBreaker init(WorkFlow flow) {
        return init(flow, null);
    }


    public static CircuitBreaker init(WorkFlow flow, Consumer<CircuitBreaker.Config> config) {

        CircuitBreaker.Config conf = new CircuitBreaker.Config();

        if (config != null) {
            config.accept(conf);
        }

        CircuitBreaker.Config oldConf = configMap.get(conf.id);
        if (oldConf == null) {
            configMap.put(conf.id, conf);
        } else {
            conf = oldConf;
        }

        CircuitBreaker circuitBreaker = new CircuitBreaker();
        circuitBreaker.config = conf;
        circuitBreaker.flow = flow;

        return circuitBreaker;
    }

    public WorkFlow apply(Consumer<WorkFlow> func) {


        this.startFlow(flow);

        func.accept(flow);

        this.endFlow(flow);


        return flow;
    }




    public static class Config {

        private String id;

        private int failCount;

        private long lastFailTime = 0;

        private int maxFailCount = 3;

        private long retryTimeGap = 10 * Time.SECOND;

        private long timeout = 3000;

        private String timeoutEventName = UUID.randomUUID().toString();


        public Config id(String id) {
            this.id = id;
            return this;
        }

        public Config maxFailCount(int maxFailCount) {
            this.maxFailCount = maxFailCount;
            return this;
        }

        public Config retryTimeGap(long time) {
            this.retryTimeGap = time;
            return this;
        }

        public Config timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        void timeout(WorkEvent e) {
            failCount++;
            lastFailTime = System.currentTimeMillis();
        }

        void error(WorkEvent e) {


            failCount++;
            lastFailTime = System.currentTimeMillis();

            System.err.println("!!!!! RAISE ERROR count== " + failCount + " " + e.getEventName());
        }

        void ok(WorkEvent e) {
            failCount = 0;
        }


        boolean check(WorkEvent event) {

            System.err.println("FAIL COUNT: " + failCount + "  max :" + maxFailCount);

            if (failCount > maxFailCount) {

                if (lastFailTime > 0) {
                    long t1 = System.currentTimeMillis();
                    long t2 = lastFailTime + retryTimeGap;

                    if (t1 > t2) {
                        lastFailTime = 0;
                        return true;
                    }
                } else {
                    lastFailTime = System.currentTimeMillis();
                    //throw new FlowProcessException("Circuit Breaker ");
                    return false;
                }


            }

            return true;
        }
    }

}
