package isocline.reflow._dummy;

import org.junit.Test;

public class HashcodeTest {

    @Test
    public void test() {
        String x="asd,m23lkj131";
        String y="asd,m23lkj131";

        System.out.println( x.hashCode());
        System.out.println( y.hashCode());
    }
}
