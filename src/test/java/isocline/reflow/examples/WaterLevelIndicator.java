package isocline.reflow.examples;

import isocline.reflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;


/**
 * This program is an indicator program which checks the water level periodically.
 * The water level grows up then Program when more often,
 * but it was a level down then when sometimes.
 *
 */
public class WaterLevelIndicator implements Work {

    private static Logger logger = LoggerFactory.getLogger(WaterLevelIndicator.class);


    private int DANGER = 150;

    private int WARN = 120;

    private int NORMAL = 100;

    private int waterLevel = NORMAL;


    public WaterLevelIndicator() throws MalformedURLException {

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


        Plan schedule = processor.task(WaterLevelIndicator.class).strictMode();
        schedule.activate();


        processor.shutdown(20 * Time.SECOND);
    }
}
