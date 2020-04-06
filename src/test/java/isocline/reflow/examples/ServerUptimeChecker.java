package isocline.reflow.examples;

import isocline.reflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * This program is an automatic server health when program.
 * Checked web server is busy then this program checks more slowly.
 * and web server has a good response time  then the program checks more often.
 *
 */
public class ServerUptimeChecker  implements Work {

    private static Logger logger = LoggerFactory.getLogger(ServerUptimeChecker.class);


    private URL url;

    private int failCount = 0;

    public ServerUptimeChecker(String strUrl) throws MalformedURLException {

        this.url = new URL(strUrl);
    }

    public long execute(WorkEvent event) throws InterruptedException {


        try {
            logger.debug(" when");
            long t1 = System.currentTimeMillis();

            long tt2 = System.nanoTime();
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            long tt3 = System.nanoTime();
            urlConn.connect();

            Thread.sleep(1000);

            long tt4 = System.nanoTime();


            InputStream is = urlConn.getInputStream();
            long tt5 = System.nanoTime();

            BufferedReader reader = new BufferedReader( new InputStreamReader( is )  );

            long tt6 = System.nanoTime();

            String line = null;
            while( ( line = reader.readLine() ) != null )  {
                //System.out.println(line);
            }
            reader.close();

            long tt7 = System.nanoTime();


            urlConn.disconnect();

            System.out.println((tt3-tt2));
            System.out.println((tt4-tt3));
            System.out.println((tt5-tt4));
            System.out.println((tt6-tt5));
            System.out.println((tt7-tt6));


            long t2 = System.currentTimeMillis();

            long timeGap = t2-t1;

            logger.debug(this.url.toString() +" OK. connection time="+timeGap);

            if(timeGap<100) {
                return 2* Time.SECOND;
            }else if(timeGap>=10 && timeGap<1000) {
                return 5* Time.SECOND;
            }else {
                return 10* Time.SECOND;
            }

        }catch (IOException ioe) {

            failCount++;

            if(failCount>10) {
                return TERMINATE;
            }
            return 30* Time.SECOND;

        }

   }

   public static void main(String[] args) throws Exception {
       FlowProcessor processor = FlowProcessorFactory.getProcessor();


       String[] urls = new String[] {"https://www.google.com","https://www.apple.com"};
       for(String url:urls) {

           ServerUptimeChecker checker = new ServerUptimeChecker( url);
           Plan schedule = processor.task(checker).on("connectTypeChange").jitter(200).strictMode();
           schedule.activate();
       }



       processor.shutdown(20* Time.SECOND);
   }
}
