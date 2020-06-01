import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        Function<Object, String> f = new Function<Object, String>(){
            public String apply (Object o) {
                return o.toString();
            }
        };
        List<Integer> l = Arrays.asList(1, 2, 3);
        Mapper<Integer, String> m = new Mapper<>();
        for (String s : m.map(l, f)) {
            System.out.println(s) ;
        }
    }
}
