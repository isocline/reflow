/*
 * Copyright 2018 The Isocline Project
 *
 * The Isocline Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package isocline.reflow.descriptor;

import isocline.reflow.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Cron style schedule description
 *
 * Provides a parser and evaluator for unix-like cron expressions. Cron
 * expressions provide the ability to specify complex time combinations such as
 * &quot;At 8:00am every Monday through Friday&quot; or &quot;At 1:30am every
 * last Friday of the month&quot;.
 *
 * Cron expressions are comprised of 6 required fields and one optional field
 * separated by white space. The fields respectively are described as follows:
 *
 * <table cellspacing="8">
 *     <caption>crontab</caption>
 * <tr>
 * <th align="left">Field Name</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Values</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Special Characters</th>
 * </tr>
 * <tr>
 * <td align="left"><code>Seconds</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Minutes</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Hours</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>0-23</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-month</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>1-31</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * ? / L W</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Month</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>0-11 or JAN-DEC</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-Week</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>1-7 or SUN-SAT</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * ? / L #</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Year (Optional)</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>empty, 1970-2199</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * </table>
 *
 *
 * <P>
 * The '*' character is used to specify all values. For example, &quot;*&quot;
 * in the minute field means &quot;every minute&quot;.
 *
 * </P><P>
 * The '?' character is allowed for the day-of-month and day-of-week fields. It
 * is used to specify 'no specific value'. This is useful when you need to
 * specify something in one of the two fields, but not the other.
 * </P><P>
 * The '-' character is used to specify ranges For example &quot;10-12&quot; in
 * the hour field means &quot;the hours 10, 11 and 12&quot;.
 * </P><P>
 * The ',' character is used to specify additional values. For example
 * &quot;MON,WED,FRI&quot; in the day-of-week field means &quot;the days Monday,
 * Wednesday, and Friday&quot;.
 * </P><P>
 * The '/' character is used to specify increments. For example &quot;0/15&quot;
 * in the seconds field means &quot;the seconds 0, 15, 30, and 45&quot;. And
 * &quot;5/15&quot; in the seconds field means &quot;the seconds 5, 20, 35, and
 * 50&quot;.  Specifying '*' before the  '/' is equivalent to specifying 0 is
 * the value to start with. Essentially, for each field in the expression, there
 * is a set of numbers that can be turned on or off. For seconds and minutes,
 * the numbers range from 0 to 59. For hours 0 to 23, for days of the month 0 to
 * 31, and for months 0 to 11 (JAN to DEC). The &quot;/&quot; character simply helps you turn
 * on every &quot;nth&quot; value in the given set. Thus &quot;7/6&quot; in the
 * month field only turns on month &quot;7&quot;, it does NOT mean every 6th
 * month, please note that subtlety.
 * </P><P>
 * The 'L' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for &quot;last&quot;, but it has different
 * meaning in each of the two fields. For example, the value &quot;L&quot; in
 * the day-of-month field means &quot;the last day of the month&quot; - day 31
 * for January, day 28 for February on non-leap years. If used in the
 * day-of-week field by itself, it simply means &quot;7&quot; or
 * &quot;SAT&quot;. But if used in the day-of-week field after another value, it
 * means &quot;the last xxx day of the month&quot; - for example &quot;6L&quot;
 * means &quot;the last friday of the month&quot;. You can also specify an offset
 * from the last day of the month, such as "L-3" which would mean the third-to-last
 * day of the calendar month. <i>When using the 'L' option, it is important not to
 * specify lists, or ranges of values, as you'll get confusing/unexpected results.</i>
 * </P><P>
 * The 'W' character is allowed for the day-of-month field.  This character
 * is used to specify the weekday (Monday-Friday) nearest the given day.  As an
 * example, if you were to specify &quot;15W&quot; as the value for the
 * day-of-month field, the meaning is: &quot;the nearest weekday to the 15th of
 * the month&quot;. So if the 15th is a Saturday, the trigger will fire on
 * Friday the 14th. If the 15th is a Sunday, the trigger will fire on Monday the
 * 16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th.
 * However if you specify &quot;1W&quot; as the value for day-of-month, and the
 * 1st is a Saturday, the trigger will fire on Monday the 3rd, as it will not
 * 'jump' over the boundary of a month's days.  The 'W' character can only be
 * specified when the day-of-month is a single day, not a range or list of days.
 * </P><P>
 * The 'L' and 'W' characters can also be combined for the day-of-month
 * expression to yield 'LW', which translates to &quot;last weekday of the
 * month&quot;.
 * </P><P>
 * The '#' character is allowed for the day-of-week field. This character is
 * used to specify &quot;the nth&quot; XXX day of the month. For example, the
 * value of &quot;6#3&quot; in the day-of-week field means the third Friday of
 * the month (day 6 = Friday and &quot;#3&quot; = the 3rd one in the month).
 * Other examples: &quot;2#1&quot; = the first Monday of the month and
 * &quot;4#5&quot; = the fifth Wednesday of the month. Note that if you specify
 * &quot;#5&quot; and there is not 5 of the given day-of-week in the month, then
 * no firing will occur that month.  If the '#' character is used, there can
 * only be one expression in the day-of-week field (&quot;3#1,6#3&quot; is
 * not valid, since there are two expressions).
 * </P>
 *
 * <P>
 * The legal characters and the names of months and days of the week are not
 * case sensitive.
 * </P>
 *
 *
 * <b>NOTES:</b>
 * <ul>
 * <li>Support for specifying both a day-of-week and a day-of-month value is
 * not complete (you'll need to use the '?' character in one of these fields).
 * </li>
 * <li>Overflowing ranges is supported - that is, having a larger number on
 * the left hand side than the right. You might do 22-2 to catch 10 o'clock
 * at night until 2 o'clock in the morning, or you might have NOV-FEB. It is
 * very important to note that overuse of overflowing ranges creates ranges
 * that don't make sense and no effort has been made to determine which
 * interpretation CronExpression chooses. An example would be
 * "0 0 14-6 ? * FRI-MON". </li>
 * </ul>
 *
 *
 *
 * @author Sharada Jambula, James House
 * @author Contributions from Mads Henderson
 * @author Refactoring from CronTrigger to CronExpression by Aaron Craven
 *
 * @author Richard D. Kim
 */
public class CronDescriptor implements PlanDescriptor {

    private final CrontabEventChecker checker;

    private final String className;


    /**
     * Default
     *
     * @param cronDescription String representation of the cron expression
     * @throws IllegalArgumentException if the string expression cannot be parsed into a valid
     */
    public CronDescriptor(String cronDescription) throws IllegalArgumentException {

        this.checker = new CrontabEventChecker();

        className = this.checker.parse(cronDescription);
    }


    @Override
    public void build(Plan planning) {

        long t1 = Time.nextMinutes();

        //System.err.println("--- "+Time.toDateFormat(t1));

        planning.startTime(t1);
        planning.interval(Time.MINUTE);
        planning.eventChecker(this.checker);


    }


    CrontabEventChecker getChecker() {
        return this.checker;
    }

    /**
     *
     */
    public static class CrontabEventChecker implements ExecuteEventChecker {
        private Checker minChk;

        private Checker hourChk;

        private Checker dayChk;

        private Checker monChk;

        private Checker weekChk;


        CrontabEventChecker() {

        }

        String parse(String text) throws IllegalArgumentException {
            String[] items = text.split(" ");

            if (items.length < 4) {
                throw new IllegalArgumentException("Invalid data");
            }
            this.minChk = new Checker(items[0], 0, 59);
            this.hourChk = new Checker(items[1], 0, 23);
            this.dayChk = new Checker(items[2], 1, 31);
            this.monChk = new Checker(items[3], 1, 12);
            this.weekChk = new Checker(items[4], 0, 7);

            if (items.length > 5) {
                return items[5];
            }

            return null;
        }

        public boolean check(long time) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new java.util.Date(time));
            int min = calendar.get(Calendar.MINUTE);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int dayOfMon = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;


            //System.out.println( dayOfWeek + " "+weekChk.when(dayOfWeek));

            return weekChk.check(dayOfWeek) && monChk.check(month) && dayChk.check(dayOfMon) && hourChk.check(hour)
                    && minChk.check(min);

        }
    }


    private static class Checker {

        private Integer[] values = null;

        private int min = 0;

        private int max = 59;

        boolean check(int value) {
            if (values == null) {
                return true;
            }

            for (int val : values) {
                if (val == value) {
                    return true;
                }
            }

            return false;
        }

        Checker(String text, int min, int max) throws IllegalArgumentException {
            this.min = min;

            this.max = max;

            if ("*".equals(text)) {
                return;
            }

            List<Integer> list = new ArrayList<>();

            String[] items = text.split(",");

            try {
                for (String item : items) {

                    int p = item.indexOf("-");
                    if (p > 0) {
                        String from = item.substring(0, p);
                        String to = item.substring(p + 1);

                        int intFrom = Integer.parseInt(from);
                        int intTo = Integer.parseInt(to);

                        for (int i = intFrom; i <= intTo; i++) {
                            list.add(i);
                        }
                        continue;

                    }


                    p = item.indexOf("/");
                    if (p > 0) {

                        String div = item.substring(p + 1);
                        int intDiv = Integer.parseInt(div);

                        for (int i = min; i < this.max; i++) {

                            if (i % intDiv == 0) {
                                list.add(i);
                            }

                        }

                        continue;
                    }

                    int num = Integer.parseInt(item);
                    list.add(num);

                }

            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("invaid data");
            }

            if (list.size() > 0) {
                values = list.toArray(new Integer[list.size()]);
            }

        }

    }


}
