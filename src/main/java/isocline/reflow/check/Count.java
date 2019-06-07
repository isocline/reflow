package isocline.reflow.check;

import isocline.reflow.flow.func.CheckFunction;

public class Count {


    public static CheckFunction max(int maxCount) {
        return event -> {
            if (event.count() <= maxCount) {
                return true;
            } else {
                return false;
            }
        };
    }


    public static CheckFunction range(int start, int max) {
        return event -> {
            if (event.count() >= start && event.count() <= max) {
                return true;
            } else {
                return false;
            }
        };
    }


}
