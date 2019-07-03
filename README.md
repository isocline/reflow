

# re:Flow [Alpha Version]
### Multi-purpose event flow processing engine for JVM,Android,IoT and Edge Computing

<img src="https://raw.github.com/isocline/reflow/master/docs/img/title.png" width="300">


[![Build Status](https://travis-ci.org/isocline/reflow.svg)](https://travis-ci.org/isocline/reflow)


**re:Flow** is a powerful integrated workflow engine that combines various workflow methods into one. 
In some cases, real-time processing may be required, but in some cases, 
it must be executed after some event processing. Or at a particular time, like a scheduler, Many things need to be done.
There are cases where other work processing methods are combined. 
For example, after checking the job every 10 minutes based on the scheduler 
If certain conditions are met, an event can be triggered to signal the start of another task Conversely, 
you can start a newly started scheduler after a specific event occurs.

It is a very inefficient task to select a library for each of these cases 
and code each time using a different API The Flow Processor can solve this problem very easily.

## Advantages

- **Optimized Dynamic Work Processor**: Clockwork is a versatile job execution tool that satisfies job execution conditions under any circumstances.
- **Self-control process**: Optimized for dynamic control environments such as various edge computing environments by dynamically changing its schedule status during job execution.
- **Elastic scheduler**:  Scheduling is similar to the Unix crontab setting method and provides various setting functions through extended API.
- **Accurate execution**: You can precisely adjust the execution in 1 ms increments aiming at the almost real-time level.
- **Easy coding**: Simple, easy to understand coding method, the code is straightforward.
- **Small footprint library**: Provides a tiny size library without compromising other libraries.

## re:Flow is not
- **Data flow** : re:Flow is process flow management but is not data flow based on DAG(Direct acycle graph)

## Download


Download [https://github.com/isocline/mvn-repo/raw/master/isocline/reflow/0.9-SNAPSHOT/reflow-0.9-20190703.053733-1.jar] or depend via Maven:
```xml
<dependency>
  <groupId>isocline</groupId>
  <artifactId>reflow</artifactId>
  <version>0.9-SNAPSHOT</version>
</dependency>

<repositories>
    <repository>
       <id>reflow</id>
       <url>https://raw.github.com/isocline/mvn-repo</url>
    </repository>
</repositories>
```

## Running Reflow

Modern Reflow versions have the following Java requirements:

 - Java v1.8 or higher, both 32-bit and 64-bit versions are supported
 - Older versions of Java are not supported



## Example

### Simple Repeater

Repeated tasks every 1 seconds

```java
import isocline.reflow.*;

public class SimpleRepeater implements Work {


    public long execute(WorkEvent event) throws InterruptedException {

        // DO YOUR WORK

        return WAIT;
    }

    @Test
    public void startMethod() throws Exception {

        Re.play(new SimpleRepeater())
                .interval(1 * Time.SECOND)
                .finishTimeFromNow(5 * Time.SECOND)
                .activate();
    }

}
```
OR 
```java
import isocline.reflow.*;

public class SimpleRepeater  {


    @Test
    public void startMethod() throws Exception {

        Re.play( (WorkEvent event) -> {
            // DO YOUR WORK
            return 10 * Time.SECOND;
        })
                .finishTimeFromNow(5 * Time.SECOND)
                .activate();
    }

}
```

Real-time processing mode: Repeats exactly in milliseconds

```java

import isocline.reflow.*;

public class PreciseRepeater implements Work {

    private static Logger logger = Logger.getLogger(PreciseRepeater.class.getName());

    private int seq = 0;

    public long execute(WorkEvent event) throws InterruptedException {

        logger.debug(activate + seq++);

        return 10; // 10 milli seconds
    }

    @Test
    public void startMethod() throws Exception {

        Re.play(new PreciseRepeater())
                .strictMode()
                .activate();
 
    }

}

```

##Output
<pre>
2019-06-16 16:00:00.000 DEBUG execute:0
2019-06-16 16:00:00.010 DEBUG execute:1
2019-06-16 16:00:00.020 DEBUG execute:2
2019-06-16 16:00:00.030 DEBUG execute:3
2019-06-16 16:00:00.040 DEBUG execute:4
2019-06-16 16:00:00.050 DEBUG execute:5
2019-06-16 16:00:00.060 DEBUG execute:6
2019-06-16 16:00:00.070 DEBUG execute:7

</pre>


### Scheduling

Repeated tasks every 1 hour

```java
import isocline.reflow.*;

public class ScheduledWork implements Work {


    public long execute(WorkEvent event) throws InterruptedException {

        // DO YOUR WORK

        return WAIT;
    }

    @Test
    public void startMethod() throws Exception {

        Re.play(new ScheduledWork())
                .interval(1 * Time.HOUR)
                .startTime("2020-04-24T09:00:00Z")
                .finishTime("2020-06-16T16:00:00Z")
                .activate();
    }

}
```

Or crontab style

```java
import isocline.reflow.*;
import isocline.reflow.descriptor.CronDescriptor;

public class ScheduledWork implements Work {


    public long execute(WorkEvent event) throws InterruptedException {

        // DO YOUR WORK

        return WAIT;
    }

    @Test
    public void startMethod() throws Exception {

        Re.play(new CronDescriptor("* 1,4-6 * * *"), new ScheduledWork())
                .finishTime("2020-06-16T16:00:00Z")
                .activate();
    }

}
```
### Execution by event




```java
import isocline.reflow.*;

public class EventReceiver implements Work {


    public long execute(WorkEvent event) throws InterruptedException {

        // DO YOUR WORK

        return WAIT;
    }

    @Test
    public void startMethod() throws Exception {


        // Receiver
        Re.play(this).on("example-event")
                .activate();

        // Emitter
        WorkEventGenerator gen = new WorkEventGenerator();
        gen.setEventName("example-event");

        Re.play(gen)
                .strictMode()
                .interval(1 * Time.SECOND)
                .startTime(Time.nextSecond())
                .finishTimeFromNow(30 * Time.SECOND)
                .activate();
    }

}
```

### Control flow


<img src="https://raw.github.com/isocline/reflow/master/docs/img/sample_flow.png" width="400"/>
<br/><br/>
 
```java
import isocline.reflow.*;

public class BasicWorkFlowTest implements FlowableWork {

    public void checkMemory() {
        log("check MEMORY");
    }

    public void checkStorage() {
        log("check STORAGE");
    }

    public void sendSignal() {
        log("send SIGNAL");
    }

    public void sendStatusMsg() {
        log("send STATUS MSG");
    }

    public void sendReportMsg() {
        log("send REPORT MSG");
    }

    public void report() {
        log("REPORT");
    }


    /**
     * control flow 
     *
    **/
    public void defineWorkFlow(WorkFlow flow) {

        WorkFlow p1 = flow.next(this::checkMemory).next(this::checkStorage);

        WorkFlow t1 = flow.wait(p1).next(this::sendSignal);
        WorkFlow t2 = flow.wait(p1).next(this::sendStatusMsg).next(this::sendReportMsg);

        flow.waitAll(t1, t2).next(this::report).finish();
    }


    @Test
    public void startMethod() {
        Re.flow(this).activate(); // Async
        
        // Re.flow(this).activate().block(); // sync mode
    }

}

```

More examples
------
Link [https://github.com/isocline/reflow/tree/master/src/test/java/isocline/reflow/examples]


API
------
javadoc [https://isocline.github.io/reflow/apidocs/]
