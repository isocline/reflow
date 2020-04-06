package isocline.reflow;


public class DataChannel {


    protected Object source;

    protected Object result;

    protected Class resultType;


    public DataChannel() {

    }

    public DataChannel(Object source) {
        this.source = source;
    }


    public DataChannel source(Object source) {
        this.source = source;
        return this;
    }


    public DataChannel result(Object result) {
        this.result = result;
        return this;
    }

    public DataChannel resultType(Class resultType) {
        this.resultType = resultType;
        return this;
    }


    public Object source() {
        return this.source;
    }


    public Object result() {
        return this.result;
    }



}
