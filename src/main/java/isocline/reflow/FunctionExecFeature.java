package isocline.reflow;

public interface FunctionExecFeature {


    FunctionExecFeature before(String... eventNames);


    FunctionExecFeature success(String... eventNames);


    FunctionExecFeature fail(String... eventNames);


    FunctionExecFeature end(String... eventNames);


    FunctionExecFeature timeout(long timeoutMillis, String... eventNames);


}
