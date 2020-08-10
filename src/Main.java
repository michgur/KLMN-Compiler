import jvm.classes.ClassFile;
import klmn.KLMN;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    /*
     *   Ordered by importance
     *   TODO: implement type-based operators
     *         scope management- should either be completely from CodeGenerator or SymbolTable
     *         reimplement language features into klmn (function calls, typed variables)
     *         make the interface nicer- selecting files, turn this into an API
     *         split KLMN into a different project using the API
     *         make an extensive README showcasing this API by creating a simple language
     * */

    /* 💩💩💩 AMAZING 💩💩💩 */
    public static void main(String[] args) throws Exception
    {
        String code = new String(Files.readAllBytes(Paths.get(args[0])));
        ClassFile file = KLMN.compile(code);

        String name = "Poop";
        Path t = Paths.get("./" + name + ".class");
        Files.write(t, file.toByteArray());

        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "java -cp ./ " + name);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = r.readLine()) != null) System.out.println(line);
    }
}
