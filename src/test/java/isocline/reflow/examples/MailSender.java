package isocline.reflow.examples;

import isocline.reflow.*;
import isocline.reflow.log.XLogger;


/**
 * https://www-01.ibm.com/support/docview.wss?uid=swg21089949
 */
public class MailSender implements Work {

    private static XLogger logger = XLogger.getLogger(MailSender.class);

    long timeUnit = Time.SECOND;

    private String email;
    private String title;
    private String content;

    private int failCount = 0;

    public MailSender(String email,String title, String content)  {
        this.email = email;
        this.title = title;
        this.content = content;

    }

    private boolean send( ) {

        return false;
    }

    public long execute(WorkEvent event) throws InterruptedException {




        if(send()) {
            return TERMINATE;
        }else {
            logger.error("send fail");
            failCount++;

            if(failCount>20) {
                logger.error("max fail count");
                return TERMINATE;
            }

            long timeGap = failCount * 15 * timeUnit;

            if(timeGap>45*timeUnit) {
                timeGap = 45 * timeUnit;
            }



            return timeGap;
        }

   }

   public static void main(String[] args) throws Exception {
       FlowProcessor processor = FlowProcessorFactory.getProcessor();


       String[] emails = new String[] {"proto@proto.com","test2@_dummy.com"};
       for(String email:emails) {

           MailSender checker = new MailSender( email, "Test", "_dummy");
           Plan schedule = processor.task(checker);
           schedule.activate();
       }



       processor.shutdown(20* Time.SECOND);
   }
}
