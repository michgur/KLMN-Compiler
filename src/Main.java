import jvm.Opcodes;
import klmn.KLMN;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main implements Opcodes
{
    /*
        Syntax Plans:
            The "new" keyword is redundant (&annoying) IMO,
                so a class can't have methods with its name (which should be a thing anyway)
            Operator overloading- YES
            No syntactic difference between primitives & classes
            Tuples- e.g. (int, string) poo = (1, "poo").
                Implicitly treat tuples of length 1 as their first value
            Lambdas- e.g. (int, string) -> int poo = (a, b) -> { return a + b.length(); }
            Classes- only add these after the previous 2 are working

        Tuples Implementation:
            -can implement as a generic linked list (problem: type information is required per instance)
            -can implement as a struct (static type info)
                (problem: a special struct has to be created for each different kind of tuple)
            I think the second solution is better for our purpose, since usually there will be more than
                a single instance for each type of tuple, so the overhead is minimal
            -can have some combination of the first two to really minimize overhead
            -can only have them as a nice syntax thing, and actually keep each value as a variable
            The last solution really sounds like the best one for memory efficiency.
            It also sounds like it'll make casting between tuples of
                length 1 and values more efficient (although the main problem of that decision is syntactic unclearness)
            Problem: funcs that return a tuple (JVM only allows returning one value).
                Maybe for this case I can use one of the first two solutions, and upon returning
                the tuple I can immediately convert it to separate variables. TODO: check the RET instruction. NVM! it's deprecated
     */

    /* 💩💩💩 AMAZING 💩💩💩 */
    public static void main(String[] args) {
        try {String src = String.join("\n", Files.readAllLines(Paths.get(args[0])));
        KLMN.compile("TEMP", src);}catch (Exception e) {e.printStackTrace();}
    }
}
