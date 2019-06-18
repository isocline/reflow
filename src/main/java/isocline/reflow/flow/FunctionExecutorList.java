package isocline.reflow.flow;

import isocline.reflow.WorkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class FunctionExecutorList {

    private String evetName;

    private AtomicInteger counter;


    private List<FunctionExecutor> functionExecutorList = new ArrayList();


    FunctionExecutorList(List<FunctionExecutor> functionExecutorList, WorkEvent event, String eventName) {
        this.functionExecutorList = functionExecutorList;

        this.counter =  event.origin().getCounter(eventName);
        this.counter.set(-1);

        this.evetName = eventName;


    }


    public int size() {
        return this.functionExecutorList.size();
    }


    public FunctionExecutor get(int index) {
        return this.functionExecutorList.get(index);
    }

    public synchronized Wrapper getNextstepFunctionExecutor() {
        final int index = this.counter.addAndGet(1);

        if (index >= this.functionExecutorList.size()) {
            this.counter.set(-1);
            return null;
        }

        FunctionExecutor functionExecutor = this.functionExecutorList.get(index);
        boolean hasNext = hasNext(index);

        return new Wrapper(functionExecutor, hasNext);

    }


    private boolean hasNext(int index) {
        final int size = this.functionExecutorList.size();


        final int nextIndex = index+1;

        if (size > 0 && nextIndex < size) {
            return true;
        }


        this.counter.set(-1);
        return false;
    }


    final static public class Wrapper {
        private FunctionExecutor functionExecutor;
        private boolean hasNext = false;

        Wrapper(FunctionExecutor functionExecutor, boolean hasNext) {
            this.functionExecutor = functionExecutor;
            this.hasNext = hasNext;
        }

        public FunctionExecutor getFunctionExecutor() {
            return this.functionExecutor;
        }

        public boolean hasNext() {
            return this.hasNext;
        }


    }
}
