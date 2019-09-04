package isocline.reflow.flow.func;

import isocline.reflow.WorkEvent;


/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a></p>

 *
 * @since 1.8
 */
@FunctionalInterface
public interface WorkEventPredicate {

    /**
     * Performs this operation on the given argument.
     *
     * @param e the input argument
     * @return if process success, return true other than false.
     */
    boolean test(WorkEvent e) ;



}
