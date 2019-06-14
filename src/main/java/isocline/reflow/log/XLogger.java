package isocline.reflow.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.logging.Level;

public class XLogger {

    private Logger log4jLogger;

    private java.util.logging.Logger javaLogger;


    private XLogger(Logger log4jLogger) {
        this.log4jLogger = log4jLogger;
    }

    private XLogger(java.util.logging.Logger javaLogger) {
        this.javaLogger = javaLogger;
    }

    public static XLogger getLogger(Class cls) {
        try {
            return new XLogger(LogManager.getLogger(cls.getName()));
        } catch (Throwable e) {
            return new XLogger(java.util.logging.Logger.getLogger(cls.getName()));
        }
    }


    public void debug(Object msg) {
        if (log4jLogger != null) {
            log4jLogger.debug(msg);
        } else if (javaLogger != null) {
            javaLogger.log(Level.FINE, msg.toString());
        }
    }

    public void info(Object msg) {
        if (log4jLogger != null) {
            log4jLogger.info(msg);
        } else if (javaLogger != null) {
            javaLogger.log(Level.INFO, msg.toString());
        }
    }

    public void warn(Object msg) {
        if (log4jLogger != null) {
            log4jLogger.warn(msg);
        } else if (javaLogger != null) {
            javaLogger.log(Level.WARNING, msg.toString());
        }
    }

    public void error(Object msg) {
        if (log4jLogger != null) {
            log4jLogger.error(msg);
        } else if (javaLogger != null) {
            javaLogger.log(Level.WARNING, msg.toString());
        }
    }

    public void error(String msg, Throwable err) {
        if (log4jLogger != null) {
            log4jLogger.error(msg,err);
        } else if (javaLogger != null) {
            javaLogger.log(Level.WARNING, msg,err);

        }
    }
}
