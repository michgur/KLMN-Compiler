import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.util.ASMifier;
import test.TEMP2;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main implements Opcodes
{
    private static String methodDescriptor(Class r, Class... params) {
        StringBuilder s = new StringBuilder().append('(');
        for (Class c : params) s.append(descriptor(c));
        return s.append(')').append(descriptor(r)).toString();
    }
    private static String descriptor(Class c) {
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

    private static void declareVar(MethodVisitor mv, int index, float val) {
        mv.visitLdcInsn(val);
        mv.visitVarInsn(FSTORE, index);
    }
    private static void printVar(MethodVisitor mv, int index) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", descriptor(PrintStream.class));
        mv.visitVarInsn(FLOAD, index);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", methodDescriptor(void.class, float.class), false);
    }

    /* 💩💩💩 AMAZING 💩💩💩 */
    public static void main(String[] args) throws Exception {
        // todo: NEXT ON THE AGENDA- reimplementing everything, controlling MAXS, and ORGANIZING FFS!
        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        cw.visit(0x34, ACC_PUBLIC, "Poop", null, descriptor(Object.class), null);
        cw.visitSource("Poop.java", null);
        {
            mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", methodDescriptor(void.class, String[].class), null, null);
            mv.visitCode();

            String code = new String(Files.readAllBytes(Paths.get(args[0])));
            TEMP2.t(mv, code);

            mv.visitInsn(RETURN);

            mv.visitMaxs(100, 100); //todo
            mv.visitEnd();
        }
        cw.visitEnd();

        try { Files.write(Paths.get("Poop.class"), cw.toByteArray()); }
        catch (IOException e) { throw new RuntimeException(e); }

        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "java -cp \"C:\\Users\\Michael\\IdeaProjects\\KLMN© Compiler\" Poop");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = r.readLine()) != null) System.out.println(line);
    }
}
