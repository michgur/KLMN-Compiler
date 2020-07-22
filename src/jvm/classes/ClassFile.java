package jvm.classes;

import jvm.AttributeInfo;
import jvm.Opcodes;
import jvm.methods.MethodInfo;
import util.ByteList;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ClassFile implements Opcodes
{
    private final String name;
    private final short acc;
    private final ConstPool constPool = new ConstPool();
    private final List<MethodInfo> methods = new ArrayList<>();
    private final List<FieldInfo> fields = new ArrayList<>();
    private final List<AttributeInfo> attributes = new ArrayList<>();
    private final ByteList data = new ByteList();
    private final short thisIndex, superIndex;
    public ClassFile(String name) { this(name, ACC_PUBLIC); }
    public ClassFile(String name, int acc) {
        this.name = name;
        this.acc = (short) acc;

        data.addInt(0xCAFEBABE);   // magic
        data.addShort(0x0000);     // minor_version
        data.addShort(0x0034);     // major_version

        thisIndex = constPool.addClass(name);
        superIndex = constPool.addClass("java/lang/Object");
//        attributes.add(new InnerClasses(this));
    }

    public ClassFile createInnerClass(String name, int acc) {
        ((InnerClasses) attributes.get(0)).add(name, acc);
        return new ClassFile(this.name + "$" + name, acc);
    }

    public void run() throws IOException, InterruptedException {
        ByteList m = new ByteList(), f = new ByteList(), a = new ByteList();
        for (MethodInfo method : methods) m.addAll(method.toByteList());
        for (FieldInfo field : fields) f.addAll(field.toByteList());
        for (AttributeInfo attrib : attributes)
            if (attrib.include()) a.addAll(attrib.toByteList());

        data.addShort(constPool.getCount());    // constant_pool_count
        data.addAll(constPool.toByteList());    // constant_pool[constant_pool_count-1]
        data.addShort(acc);                     // access_flags
        data.addShort(thisIndex);               // this_class
        data.addShort(superIndex);              // super_class
        data.addShort(0x0000);               // interfaces_count
        data.addShort(fields.size());           // fields_count
        data.addAll(f);                         // fields[fields_count]
        data.addShort(methods.size());          // methods_count
        data.addAll(m);                         // methods[methods_count]
        data.addShort(attributes.size());       // attributes_count
        data.addAll(a);                         // attributes[attributes_count]

        Path t = Paths.get("./" + name + ".class");
        Files.write(t, data.toByteArray());

        ProcessBuilder builder = new ProcessBuilder(
        "cmd.exe", "/c", "java -cp \"C:\\Users\\micha\\IdeaProjects\\KLMN-Compiler\" Poop");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = r.readLine()) != null) System.out.println(line);
//        Files.delete(t); // comment line to check .class file
    }

    public ConstPool getConstPool() { return constPool; }

    public List<MethodInfo> getMethods() { return methods; }
    public List<FieldInfo> getFields() { return fields; }
    public List<AttributeInfo> getAttributes() { return attributes; }

    public String getName() { return name; }
}
