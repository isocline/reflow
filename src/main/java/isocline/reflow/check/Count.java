package isocline.reflow.check;

import isocline.reflow.flow.func.CheckFunction;

public class Count {


    public static CheckFunction max(int maxCount) {
        return event -> event.count() <= maxCount;
    }


    public static CheckFunction range(int start, int max) {
        return event -> event.count() >= start && event.count() <= max;
    }


}
