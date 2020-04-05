package isocline.reflow.flow.pattern;

import isocline.reflow.WorkEvent;
import isocline.reflow.WorkFlow;
import isocline.reflow.flow.CustomWorkFlow;
import isocline.reflow.flow.CustomWorkFlowBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CircuitBreaker extends CustomWorkFlow implements CustomWorkFlowBuilder {

    private Logger logger = LoggerFactory.getLogger(CircuitBreaker.class);

    private CircuitBreaker instance;

    private static Map<String, CircuitBreaker> instanceMap = new HashMap<>();
    private WorkFlow workFlow;

    private int failCount = 0;



    private long lastFailDatetime = 0;

    private long timeout = 1000;

    private int maxFailCount = 5;

    private long retryCheckTimeout = 1000*10;

    private long returnToNormalTimeout = 1000 * 60 * 10;


    private CircuitBreaker() {

        super();
    }

    private CircuitBreaker(WorkFlow flow, CircuitBreaker instance) {

        super(flow);
        this.workFlow = flow;
        this.instance = instance;

        start();
    }

    public static CircuitBreaker init(String keyName) {

        return init(keyName, null);
    }

    public static CircuitBreaker init(String keyName, Consumer<CircuitBreaker> builderFunction) {

        CircuitBreaker instance = instanceMap.computeIfAbsent(keyName, k -> new CircuitBreaker());

        if (builderFunction != null)
            builderFunction.accept(instance);


        return instance;
    }


    @Override
    public CustomWorkFlow build(WorkFlow workFlow) {
        return new CircuitBreaker(workFlow, this);
    }


    private void start() {

        workFlow.branch(instance::isClosedCircuit);
        workFlow.wait("close").fireEvent("timeout", timeout);
    }

    @Override
    public void closePattern() {
        workFlow.end();


        workFlow.wait("error::*").accept(instance::chk).end();
        workFlow.wait("timeout").accept(instance::chk).end();
        workFlow.wait("open").accept(instance::open).end();
    }

    public int getMaxFailCount() {
        return maxFailCount;
    }

    public void setMaxFailCount(int maxFailCount) {
        this.maxFailCount = maxFailCount;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String isClosedCircuit(WorkEvent e) {


        if (failCount > this.maxFailCount) {
            long timeGap = System.currentTimeMillis() - lastFailDatetime;

            if (timeGap > returnToNormalTimeout) {
                failCount--;


                logger.debug("[CHK] *1 CLOSE");
                return "close";

            } else if (timeGap > retryCheckTimeout) {

                if (timeGap % 5 == 0) {
                    failCount--;

                    logger.debug("[CHK] *2 CLOSE");
                    return "close";
                }
            }
        } else {
            logger.debug("[CHK] *3 CLOSE " + failCount);
            return "close";
        }

        logger.debug("[CHK] * OPEN " + failCount);

        return "open";

    }


    public void chk(WorkEvent event) {
        failCount++;
        lastFailDatetime = System.currentTimeMillis();
        logger.debug("check error [{}]   failCount:{}", event.getFireEventName(), failCount);

    }

    public void open(WorkEvent event) {

        logger.debug("open > " + event.getFireEventName());
    }


}