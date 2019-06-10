package isocline.reflow;

public interface ActivatedPlan extends Plan {


    void inactive();

    ActivatedPlan block();

    ActivatedPlan block(long timeout);

    Throwable getError();

    void setError(Throwable error);
}
