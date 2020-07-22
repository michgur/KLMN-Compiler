package jvm;

import jdk.internal.org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JVMType implements Opcodes
{
    private static final Map<String, JVMType> types = new HashMap<>();

    public static final JVMType VOID = new JVMType("V", null, 0);
    public static final JVMType BYTE = new JVMType("B", T_BYTE, 0);
    public static final JVMType CHARACTER = new JVMType("C", T_CHAR, 0);
    public static final JVMType INTEGER = new JVMType("I", T_INT, 0);
    public static final JVMType FLOAT = new JVMType("F", T_FLOAT, 0);
    public static final JVMType DOUBLE = new JVMType("D", T_DOUBLE, 0);
    public static final JVMType LONG = new JVMType("J", T_LONG, 0);
    public static final JVMType SHORT = new JVMType("S", T_SHORT, 0);
    public static final JVMType BOOLEAN = new JVMType("Z", T_BOOLEAN, 0);

    /* The JVM representation of this type */
    private String descriptor;
    /* Same as descriptor, but primitive types are represented by integer opcodes */
    private Object id;
    /* Number of dimensions for this type. 0 If this is not an array type */
    private int dim;

    private JVMType(String descriptor, Object id, int dim) {
        this.descriptor = descriptor;
        this.id = id;
        this.dim = dim;
    }

    public String getDescriptor() { return descriptor; }
    public Object getID() { return (id == null) ? descriptor : id; }
    public boolean isArrayType() { return dim > 0; }
    public int getArrayDimensions() { return dim; }

    public static JVMType refType(String name) {
        String descriptor = "L" + name.replace('.', '/') + ";";
        if (types.containsKey(descriptor)) return types.get(descriptor);

        JVMType type = new JVMType(descriptor, null, 0);
        types.put(descriptor, type);
        return type;
    }
    public static JVMType arrayType(JVMType baseType, int dim) {
        String descriptor = String.join("", Collections.nCopies(dim, "[")) + baseType.descriptor;
        if (types.containsKey(descriptor)) return types.get(descriptor);

        JVMType type = new JVMType(descriptor, null, baseType.dim + dim);
        types.put(descriptor, type);
        return type;
    }

    public JVMType getBaseType() {
        if (dim == 0) throw new RuntimeException("JVM Type " + descriptor + " has no base type");
        String baseDescriptor = descriptor.substring(1);

        if (types.containsKey(baseDescriptor)) return types.get(baseDescriptor);
        else {
            JVMType baseType = new JVMType(baseDescriptor, null, dim - 1);
            types.put(baseDescriptor, baseType);
            return baseType;
        }
    }

    public static String typeDescriptor(Class c) {
        String name = c.getCanonicalName();
        int dim = name.length() - name.replace("[", "").length();
        if (dim > 0) name = name.substring(0, name.indexOf('['));
        switch (name) {
            case "void": name = "V"; break;
            case "byte": name = "B"; break;
            case "char": name = "C"; break;
            case "int": name = "I"; break;
            case "float": name = "F"; break;
            case "long": name = "J"; break;
            case "double": name = "D"; break;
            case "short": name = "S"; break;
            case "boolean": name = "Z"; break;
            default: name = 'L' + name.replace('.', '/') + ';';
        }
        return String.join("" ,Collections.nCopies(dim, "[")) + name;
    }

    public static String methodDescriptor(Method m) {
        StringBuilder s = new StringBuilder().append('(');
        for (Class c : m.getParameterTypes()) s.append(typeDescriptor(c));
        return s.append(')').append(typeDescriptor(m.getReturnType())).toString();
    }

    public static String methodDescriptor(Class ret, Class... params) {
        StringBuilder s = new StringBuilder().append('(');
        for (Class c : params) s.append(typeDescriptor(c));
        return s.append(')').append(typeDescriptor(ret)).toString();
    }

    public static String methodDescriptor(JVMType ret, JVMType... params) {
        StringBuilder s = new StringBuilder().append('(');
        for (JVMType t : params) s.append(t.descriptor);
        return s.append(')').append(ret.descriptor).toString();
    }

    @Override
    public boolean equals(Object obj) { return obj instanceof JVMType && id == ((JVMType) obj).id; }
}
