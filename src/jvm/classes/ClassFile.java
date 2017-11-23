package jvm.classes;

import jvm.AttributeInfo;
import jvm.Opcodes;
import jvm.methods.Code;
import jvm.methods.MethodInfo;
import util.ByteList;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ClassFile implements Opcodes
{
    private String name;
    private short acc;
    private ConstPool constPool = new ConstPool();
    private List<MethodInfo> methods = new ArrayList<>();
    private List<FieldInfo> fields = new ArrayList<>();
    private List<AttributeInfo> attributes = new ArrayList<>();
    private ByteList data = new ByteList();
    private short thisIndex, superIndex;
    public ClassFile(String name) { this(name, ACC_PUBLIC); }
    public ClassFile(String name, int acc) {
        this.name = name;
        this.acc = (short) acc;

        data.addInt(0xCAFEBABE);   // magic
        data.addShort(0x0000);     // minor_version
        data.addShort(0x0034);     // major_version

        thisIndex = constPool.addClass(name);
        superIndex = constPool.addClass("java/lang/Object");
        attributes.add(new InnerClasses(this));
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
        Process p = Runtime.getRuntime().exec(new String[] { "java", "-cp", "home/michael/IdeaProjects/KLMN\u00A9 Compiler/klmn.jar:.", "TEMP" });
        p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) System.out.println(line);
//        Files.delete(t); // comment line to check .class file
    }

    public ConstPool getConstPool() { return constPool; }

    public List<MethodInfo> getMethods() { return methods; }
    public List<FieldInfo> getFields() { return fields; }
    public List<AttributeInfo> getAttributes() { return attributes; }

    public String getName() { return name; }
}
