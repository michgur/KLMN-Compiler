package codegen;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

import java.io.PrintStream;
import java.util.Collections;

public class JVMUtil implements Opcodes
{
    public static String methodDescriptor(Class r, Class... params) {
        StringBuilder s = new StringBuilder().append('(');
        for (Class c : params) s.append(descriptor(c));
        return s.append(')').append(descriptor(r)).toString();
    }
    // mein got. a MAJOR setback. should've used fckn git
    public static String descriptor(Class c) {
        String type = c.getCanonicalName().replaceAll("[]\\[]", "");
        int dim = 0;
        while ((c = c.getComponentType()) != null) dim++;
        switch (type) {
            case "boolean": type = "Z"; break;
            case "char": type = "A"; break;
            case "byte": type = "B"; break;
            case "short": type = "S"; break;
            case "int": type = "I"; break;
            case "long": type = "J"; break;
            case "float": type = "F"; break;
            case "double": type = "D"; break;
            case "void": type = "V"; break;
            default: type = 'L' + type.replace('.', '/') + ';';
        } return String.join("", Collections.nCopies(dim, "[")) + type;
    }

    public static void declareVar(MethodVisitor mv, int index, float val) {
        mv.visitLdcInsn(val);
        mv.visitVarInsn(FSTORE, index);
    }
    public static void printVar(MethodVisitor mv, int index) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", descriptor(PrintStream.class));
        mv.visitVarInsn(FLOAD, index);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", methodDescriptor(void.class, float.class), false);
    }
}
