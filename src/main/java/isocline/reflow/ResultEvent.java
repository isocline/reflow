package isocline.reflow;

public interface ResultEvent {

    /**
     * Blocking until internal work process complete.
     *
     */
    void block();

    /**
     * Returns the object bound with the specified name in this session,
     * or null if no object is bound under the name.
     *
     * @param key a string specifying the name of the object
     * @return the object with the specified name
     */
    Object get(String key);


    /**
     * Returns error information. Null is returned in normal state.
     *
     * @return an instance of Throwable
     */
    Throwable getThrowable();



    DataChannel dataChannel();




}
