import jvm.ClassCode;
import jvm.ClassFile;

import java.io.PrintStream;

import static jvm.ClassFile.*;
import static jvm.Instruction.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    /* 💩💩💩 AMAZING 💩💩💩 */
    public static void main(String[] args) throws Exception {
        // all KLMN-related code has been temporarily moved to test.TEMP

        ClassFile poop = new ClassFile("Poop");
        short f = poop.addFieldRefConst(poop.addClassConst(poop.addStringConst("java/lang/System")),
                poop.addNameTypeConst(poop.addStringConst("out"), poop.addStringConst(objectType(PrintStream.class))));
        short f1 = poop.addStringRefConst(poop.addStringConst("hello world"));
        short f2 = poop.addMethodRefConst(poop.addClassConst(poop.addStringConst("java/io/PrintStream")),
                poop.addNameTypeConst(poop.addStringConst("println"),
                poop.addStringConst(ClassFile.methodDesc(TYPE_VOID, objectType(String.class)))));
        ClassCode m = new ClassCode();
        m.add(GETSTATIC, f);
        m.add(LDC, (byte) f1);
        m.add(INVOKEVIRTUAL, f2);
        m.add(RETURN);
        poop.addMethod(ACC_PUBLIC | ACC_STATIC, "main",
            methodDesc(TYPE_VOID, arrayType(objectType(String.class))), m.getRawCode());

        poop.write();
    }
}
