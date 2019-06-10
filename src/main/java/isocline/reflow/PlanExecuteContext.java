package isocline.reflow;

public class PlanExecuteContext {

    private Plan plan;

    private boolean isUserEvent = false;

    private WorkEvent workEvent;


    PlanExecuteContext(Plan plan, boolean isUserEvent, WorkEvent event) {

        this.plan = plan;
        this.isUserEvent = isUserEvent;

        this.workEvent = event;

            /*
            if(event!=null) {
                event.setPlan(plan);

            }
            */
    }


    boolean isExecuteImmediately() {
        if (isUserEvent) {
            this.isUserEvent = false;

            return true;
        }

        return false;
    }

    Plan getPlan() {

        //if (this.contextId == this.plan.contextCheckId)
        {
            return plan;
        }

        //return null;

    }

    WorkEvent getWorkEvent() {
        return this.workEvent;
    }
}
