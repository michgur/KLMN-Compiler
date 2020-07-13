import jdk.internal.org.objectweb.asm.*;
import klmn.KLMN;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static codegen.JVMUtil.descriptor;
import static codegen.JVMUtil.methodDescriptor;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main implements Opcodes
{
    /* 💩💩💩 AMAZING 💩💩💩 */
    public static void main(String[] args) throws Exception {
        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        cw.visit(0x34, ACC_PUBLIC, "Poop", null, descriptor(Object.class), null);
        cw.visitSource("Poop.java", null);
        {
            mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", methodDescriptor(void.class, String[].class), null, null);
            mv.visitCode();

            String code = new String(Files.readAllBytes(Paths.get(args[0])));
            int ML = KLMN.compile(mv, code);

            mv.visitInsn(RETURN);

            mv.visitMaxs(100, ML); //todo
            mv.visitEnd();
        }
        cw.visitEnd();

        try { Files.write(Paths.get("Poop.class"), cw.toByteArray()); }
        catch (IOException e) { throw new RuntimeException(e); }

        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "java -cp \"C:\\Users\\micha\\IdeaProjects\\KLMN-Compiler\" Poop");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = r.readLine()) != null) System.out.println(line);
    }
}
