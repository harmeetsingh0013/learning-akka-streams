package actors;

import java.util.Arrays;

public class Java8Stream {

    public static void main(String[] args) {
        Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .stream()
                .peek(x -> System.out.println("pre-map: "+x))
                .map(x -> x * 3)
                .peek(x -> System.out.println("post-map: "+x))
                .filter(x -> x % 2 == 0)
                .forEach(x -> System.out.println("done: "+ x));
    }
}
