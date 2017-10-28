import jvm.Opcodes;
import jvm.classes.ClassFile;
import jvm.classes.ConstPool;
import jvm.methods.Code;
import jvm.methods.Frame;
import jvm.methods.MethodInfo;
import test.MethodWriter;
import test.TEMP2;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.TreeSet;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main implements Opcodes
{
    /* 💩💩💩 AMAZING 💩💩💩 */
    public static void main(String[] args) throws Exception {
        String src = String.join("\n", Files.readAllLines(Paths.get(args[0])));
        ClassFile cf = new ClassFile("TEMP");
        ConstPool cp = cf.getConstPool();
        MethodInfo main = new MethodInfo(cf, "main", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "V", "[Ljava/lang/String;");
        TEMP2.t(cp, new MethodWriter(cp, main), src);
        cf.addMethod(main);
        cf.run();
    }
}
