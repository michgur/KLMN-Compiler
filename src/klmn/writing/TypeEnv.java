package klmn.writing;

import java.util.*;

public class TypeEnv
{
    private Set<Type> types = new HashSet<>();

    public Type add(String name, String descriptor) { // todo- arrays
        Type t = new Type(name, descriptor);
        types.add(t);
        return t;
    }

    public Type getForName(String name) {
        for (Type t : types) if (t.name.equals(name)) return t;
        throw new RuntimeException("No defined type with name " + name);
    }
    public Type getForDescriptor(String desc) {
        for (Type t : types) if (t.desc.equals(desc)) return t;
        throw new RuntimeException("No defined type with descriptor " + desc);
    }

    public String getName(String desc) { return getForDescriptor(desc).name; }
    public String getDescriptor(String name) { return getForName(name).desc; }

    {
        add("int", "I");
        add("long", "J");
        add("float", "F");
        add("double", "D");
        add("string", "Ljava/lang/String;");
        add("boolean", "Z");
        add("shit", "[Ljava/lang/String;");
        add("void", "V");
    }

    public static class Type
    {
        private String desc, name;
        private int dim; // -ensions
        private Type(String name, String desc) { this(name, desc, 1); }
        private Type(String name, String desc, int dim) {
            this.name = name;
            this.desc = desc;
            this.dim = dim;
        }

        public String getName() { return name; }
        public String getDescriptor() { return desc; }

        public boolean isArray() { return dim > 1; }
        public int getDimensions() { return dim; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Type type = (Type) o;
            return dim == type.dim && desc.equals(type.desc) && name.equals(type.name);
        }

        @Override
        public int hashCode() { return 31 * (31 * desc.hashCode() + name.hashCode()) + dim; }
    }
}
