import jvm.JVMType;
import klmn.KLMN;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    /*
    *   Ordered by importance
    *   TODO: merge MethodGenerator and CodeGenerator -DONE
    *         implement type-based operators
    *         scope management- should either be completely from CodeGenerator or SymbolTable
    *         SymbolTable needs to index variables differently- numeral index for locals, field information for fields -DONE
    *         Visitors- perhaps should correspond to a specific production, not just a Symbol -DONE
    *         reimplement language features into klmn (function calls, typed variables)
    *         make the interface nicer- selecting files, turn this into an API
    *         split KLMN into a different project using the API
    *         make an extensive README showcasing this API by creating a simple language
    * */

    /* 💩💩💩 AMAZING 💩💩💩 */
    public static void main(String[] args) throws Exception {
        String code = new String(Files.readAllBytes(Paths.get(args[0])));
        KLMN.compile(code);
    }
}
