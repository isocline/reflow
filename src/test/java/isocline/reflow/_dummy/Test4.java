package isocline.reflow._dummy;

public class Test4<T> {


    public <T, D> void test(D t, Test4_2<D> x) {


    }

    public static void main(String[] args) {

        Test4<String> t = new Test4();

        t.test(Test4_m2.instance(), e -> {
            e.test123();


        });

    }
}
