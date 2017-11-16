package jvm.classes;

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
    private ConstPool constPool = new ConstPool();
    private List<MethodInfo> methods = new ArrayList<>();
    private List<FieldInfo> fields = new ArrayList<>();
    private ByteList data = new ByteList();
    private short thisIndex, superIndex;
    public ClassFile(String name) {
        this.name = name;

        data.addInt(0xCAFEBABE);   // magic
        data.addShort(0x0000);     // minor_version
        data.addShort(0x0034);     // major_version

        thisIndex = constPool.addClass(name);
        superIndex = constPool.addClass("java/lang/Object");
    }

    public void run() throws IOException, InterruptedException {
        ByteList m = new ByteList(), f = new ByteList();
        for (MethodInfo method : methods) m.addAll(method.toByteList());
        for (FieldInfo field : fields) f.addAll(field.toByteList());

        data.addShort(constPool.getCount());    // constant_pool_count
        data.addAll(constPool.toByteList());    // constant_pool[constant_pool_count-1]
        data.addShort(Opcodes.ACC_PUBLIC);      // access_flags
        data.addShort(thisIndex);               // this_class
        data.addShort(superIndex);              // super_class
        data.addShort(0x0000);               // interfaces_count
        data.addShort(fields.size());           // fields_count
        data.addAll(f);                         // fields[fields_count]
        data.addShort(methods.size());          // methods_count
        data.addAll(m);                         // methods[methods_count]
        data.addShort(0x0000);               // attributes_count

        Path t = Paths.get("./" + name + ".class");
        Files.write(t, data.toByteArray());
//        Process p = Runtime.getRuntime().exec("java " + name);
//        p.waitFor();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//        String line;
//        while ((line = reader.readLine()) != null) System.out.println(line);
//        Files.delete(t); // comment line to check .class file
    }

    public ConstPool getConstPool() { return constPool; }

    public List<MethodInfo> getMethods() { return methods; }
    public List<FieldInfo> getFields() { return fields; }
}
