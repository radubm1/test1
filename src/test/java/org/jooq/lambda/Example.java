package org.jooq.lambda;

import java.math.BigDecimal;

import static org.jooq.lambda.tuple.Tuple.tuple;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Random;
import org.jooq.lambda.tuple.Tuple3;

import org.jooq.lambda.tuple.Tuple4;

public class Example {
    public static void main(String[] args) {
        List<Person> personsList = new ArrayList<>();

        personsList.add(new Person("John", "Doe", 25, 1.80, 80));
        personsList.add(new Person("Jane", "Doe", 30, 1.69, 60));
        personsList.add(new Person("John", "Smith", 35, 174, 70));

        Tuple4<Long, Optional<Integer>, Optional<Double>, Optional<Double>> r1 =
        Seq.seq(personsList)
           .filter(p -> p.getFirstName().equals("John"))
           .collect(
                Agg.count(),
                Agg.max(Person::getAge),
                Agg.min(Person::getHeight),
                Agg.avg(Person::getWeight)
           );


        System.out.println(r1);
        System.out.println();

        System.out.println(
            Seq.of("a", "a", "a", "b", "c", "c", "d", "e")
               .window(naturalOrder())
               .map(w -> tuple(
                    w.value(),
                    w.rowNumber(),
                    w.rank(),
                    w.denseRank()
               ))
               .format()
        );
        System.out.println();
        
        
        System.out.println(
            Seq.of("a", "a", "a", "b", "c", "c", "d", "e")
               .window(naturalOrder())
               .map(w -> tuple(
                    w.value(),
                    w.count(),
                    w.median(),
                    w.lead(),
                    w.lag(),
                    w.toString()
               ))
               .format()
        );
        System.out.println();
        
        
        BigDecimal currentBalance = new BigDecimal("19985.81");
        
        System.out.println(
            Seq.of(
                    tuple(9997, "2014-03-18", new BigDecimal("99.17")),
                    tuple(9981, "2014-03-16", new BigDecimal("71.44")),
                    tuple(9979, "2014-03-16", new BigDecimal("-94.60")),
                    tuple(9977, "2014-03-16", new BigDecimal("-6.96")),
                    tuple(9971, "2014-03-15", new BigDecimal("-65.95")))
               .window(Comparator.comparing((Tuple3<Integer, String, BigDecimal> t) -> t.v1, reverseOrder()).thenComparing(t -> t.v2), Long.MIN_VALUE, -1)
               .map(w -> w.value().concat(
                    currentBalance.subtract(w.sum(t -> t.v3).orElse(BigDecimal.ZERO))
               ))
               .format()
        );
        System.out.println();
        
        System.out.println(
        Seq.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            .window(i -> i % 2, -1, 1)
            .map(w -> tuple(w.value(), w.sum()))
                
            .format());

        Database db = new Database();
        db.Initialize();

//Sa se listeze numele, camera si specializarea studentilor cu media generala >= 8.50
        List<Student> result = db.Studenti.stream().filter(s -> s.major.equals("Math") && s.GPA>=8.5).collect(Collectors.toList());
        result.forEach(s->{System.out.println("name=" + s.name + " dorm=" + s.dorm);});

//Sa se listeze profesorii si catedrele de care apartin, pentru cei angajati inainte de 1990 cu un salariu mai mic de 8000 lei
        List<Professor> result1 = db.Profesori.stream().filter(p -> p.salary<=8000 && p.year<=1990).collect(Collectors.toList());
        result1.forEach(p->{System.out.println("name=" + p.name);});

//Afisati profesorii care au salariul mai mare decat al sefului de catedra
        //db.Profesori.stream()
        System.out.println(Seq.seq(db.Profesori)

                .sorted((p1, p2) -> Long.compare(p1.salary, p2.salary))
                .flatMap(v1 -> db.Decani.stream()
                        .filter(v2 -> Objects.equals(v1.dept, v2.dept) && db.Profesori.stream()
                                .anyMatch(t -> v1.salary>t.salary && t.name.equals(v2.name)))
                        .map(v2 -> tuple(v1.name, v2.name)))
                .format());
                //.forEach(System.out::println);

//Afisati profesorii cu cel mai mare salariu din departament:
        db.Profesori.stream().filter(p->db.Profesori.stream().anyMatch(t->t.salary<p.salary && t.dept.equals(p.dept))).forEach(p->{System.out.println("name=" + p.name);});

//result2.forEach(p->{System.out.println("name=" + p.name);});.


        List<Integer> hand = new ArrayList<Integer>();
        Random rnd = new Random();
        boolean k = false;
        Integer abc=0;
        int j=0;
        Integer MAX = 0;
        Integer MIN = 0;

        while (!k) {
            for (int i = 0; i < 5; i++) {
                abc = rnd.nextInt(13) + 2;
                hand.add(abc);
            }

            hand.sort(Comparator.naturalOrder());

            System.out.println(++j + ". These are your cards: " + hand);

            MAX = Seq.seq(hand).max().get();
            MIN = Seq.seq(hand).min().get();

            if (Seq.seq(hand).groupBy(i -> i).values().stream().filter(u -> u.size() >= 2).collect(Collectors.toList()).isEmpty())
                //straight
                if (((MAX - MIN + 1) == 5 ? true : false)) {
                    System.out.println("Great! You've got Straight!");
                    k = true;
                } else
                    //high card
                    System.out.println("Your highest card is: " + Seq.seq(hand).max().get());
            else {
                //one pair, two pairs, three of a kind, four of a kind & full house
                System.out.print("Nice! You've got: ");
                Seq.seq(hand).groupBy(i -> i).values().stream().filter(u -> u.size() >= 2).collect(Collectors.toList()).forEach(System.out::println);
            }
            hand.clear();
        }
      }
}

class Student{
    String name;
    String dorm;
    String major;
    double GPA;
    public Student(String a_name, String a_dorm, String a_major, double a_GPA){
        name=a_name; dorm=a_dorm; major=a_major; GPA=a_GPA;
    }
}

class Professor{
    String name;
    String dept;
    long salary;
    int year;
    public Professor(String a_name, String a_dept, long a_salary, int a_year){
        name=a_name; dept=a_dept; salary=a_salary; year=a_year;
    }
}

class Chair{
    String name;
    String dept;
    public Chair(String a_name, String a_dept){
        name=a_name; dept=a_dept;
    }
}

class Database{
    List<Student> Studenti;
    List<Professor> Profesori;
    List<Chair> Decani;
    public void Initialize(){
        Studenti = new ArrayList<Student>();
        Profesori = new ArrayList<Professor>();
        Decani = new ArrayList<Chair>();
        Decani.add(new Chair("Iosipescu","Math"));
        Decani.add(new Chair("Radulescu","CS"));
        Profesori.add(new Professor("Georgescu","CS",5000,1999));
        Profesori.add(new Professor("Iosipescu","Math",3000,2004));
        Profesori.add(new Professor("Radulescu","CS",7000,2000));
        Profesori.add(new Professor("Marinescu","Math",6000,1998));
        Studenti.add(new Student("Ionescu", "A5", "CS", 9.5));
        Studenti.add(new Student("Marinescu", "A3", "Math", 9.0));
        Studenti.add(new Student("Popescu", "A4", "CS", 8.5));
        Studenti.add(new Student("Vasilescu", "A5", "Math", 7.5));
    }

}

class Person {

    private String firstName;
    private String lastName;
    private int    age;
    private double height;
    private double weight;

    public Person(String firstName, String lastName, int age, double height, double weight) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.height = height;
        this.weight = weight;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

}
