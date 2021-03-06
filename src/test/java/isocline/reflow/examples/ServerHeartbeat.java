package isocline.reflow.examples;

import isocline.reflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;


/**
 * 인간의 심장처럼 서버의 사용량이 폭주하거나 이상이 있을 수록 신호를 주기를 짧게 처리한다
 *
 */
public class ServerHeartbeat implements Work {

    private static Logger logger = LoggerFactory.getLogger(ServerHeartbeat.class);


    private int DANGER = 150;

    private int WARN = 120;

    private int NORMAL = 100;

    private int waterLevel = NORMAL;


    public ServerHeartbeat() throws MalformedURLException {

    }

    private int checkWaterLevel() {

        int x = 400000 / (40000 - waterLevel * waterLevel);


        int gap = (int) (Math.random() * 100 - x) / 5;

        waterLevel = waterLevel + gap;

        return waterLevel;

    }


    public long execute(WorkEvent event) throws InterruptedException {

        int waterLevel = checkWaterLevel();

        logger.info("LEVEL : " + waterLevel);

        if (waterLevel >= DANGER) {
            return 1 * Time.SECOND;

        } else if (waterLevel >= WARN) {
            return 2 * Time.SECOND;

        } else {
            return 3 * Time.SECOND;
        }


    }

    public static void main(String[] args) throws Exception {
        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.task(ServerHeartbeat.class).strictMode();
        schedule.activate();


        processor.shutdown(20 * Time.SECOND);
    }
}
