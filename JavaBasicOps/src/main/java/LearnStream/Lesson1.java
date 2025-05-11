package LearnStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Lesson1 {
    public static void main(String[] args) {
//        Stream.generate(Math::random).
//                limit(3).
//                forEach(System.out::println);
//        Stream.of("DJW", "YJYX", "LvGe").
//                forEach(System.out::println);
//        Stream.iterate(0, x-> x+3).limit(4)
//                .forEach(System.out::println);

        List<Person> personList = new ArrayList<Person>();
        personList.add(new Person("Tom", 8900, "male", "New York"));
        personList.add(new Person("Jack", 7000, "male", "Washington"));
        personList.add(new Person("Lily", 7800, "female", "Washington"));
        personList.add(new Person("Anni", 8200, "female", "New York"));
        personList.add(new Person("Owen", 9500, "male", "New York"));
        personList.add(new Person("Alisa", 7900, "female", "New York"));
        

        List<Integer> list = Arrays.asList(1, 3, 2, 8, 11, 4);
        Integer reduce = list.stream().reduce(1, Integer::max);
        System.out.println(reduce);


    }
}


