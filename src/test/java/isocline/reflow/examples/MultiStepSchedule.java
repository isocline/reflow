package isocline.reflow.examples;

import isocline.reflow.*;
import isocline.reflow.event.WorkEventFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This program is an indicator program which checks the water level periodically.
 * The water level grows up then Program when more often,
 * but it was a level down then when sometimes.
 */
public class MultiStepSchedule   {

    private static Logger logger = LoggerFactory.getLogger(MultiStepSchedule.class);

    public static void main(String[] args) throws Exception {
        FlowProcessor processor = FlowProcessorFactory.getProcessor();


        Plan schedule = processor.task(Step1Schedule.class).strictMode();
        schedule.activate();

        schedule = processor.task(Step2Schedule.class).on("fireEvent");
        schedule.activate();


        processor.shutdown(20 * Time.SECOND);
    }

    public static class Step1Schedule implements Work {

        private int count = 0;

        @Override
        public long execute(WorkEvent event) throws InterruptedException {
            count++;

            logger.debug("count="+count);

            if (count > 5) {

                event.getActivity().getFlowProcessor().emit(WorkEventFactory.createOrigin("fireEvent"));

                return TERMINATE;
            } else {
                return 1 * Time.SECOND;
            }

        }
    }

    public static class Step2Schedule implements Work {


        @Override
        public long execute(WorkEvent event) throws InterruptedException {

            logger.debug(event.getEventName() + " XX");

            if("fireEvent".equals(event.getEventName())) {
                return TERMINATE;
            }


            return 3* Time.SECOND;


        }
    }
}
