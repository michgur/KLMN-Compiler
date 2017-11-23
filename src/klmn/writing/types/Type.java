package klmn.writing.types;

import klmn.nodes.TypeNode;

import java.util.Arrays;
import java.util.Collections;

public class Type
{
    private String desc, name;
    // todo: move dim to a separate array type
    private int dim; // -ensions
    Type(String name, String desc) { this(name, desc, 0); }
    Type(String name, String desc, int dim) {
        this.name = name;
        if (dim > 0) this.name += String.join("", Collections.nCopies(dim, "[]"));
        this.desc = desc;
        this.dim = dim;
    }

    public String getName() { return name; }
    public String getDescriptor() { return desc; }

    public boolean isArray() { return dim > 0; }
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

    @Override
    public String toString() { return "Type " + name + " (descriptor: " + desc + ')'; }

    public static class Tuple extends Type
    {
        private Type[] types;
        public Tuple(Type... types) {
            super(name(types), "tupie poopy");
            this.types = types;
        }

        private static String name(Type... types) {
            StringBuilder s = new StringBuilder().append('(');
            for (Type p : types) s.append(p.name).append(", ");
            return s.deleteCharAt(s.length() - 1).deleteCharAt(s.length() - 1).append(')').toString();
        }

        public Type[] getTypes() { return types; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tuple type = (Tuple) o;
            return Arrays.equals(types, type.types);
        }
    }

    public static class Function extends Type
    {
        private Type ret;
        private Type[] params;
        public Function(Type ret, Type... params) {
            super(name(params, ret), descriptor(params, ret));
            this.ret = ret;
            this.params = params;
        }

        private static String name(Type[] params, Type ret) {
            StringBuilder s = new StringBuilder().append('(');
            for (Type p : params) s.append(p.name).append(", ");
            if (params.length > 0) s.deleteCharAt(s.length() - 1).deleteCharAt(s.length() - 1);
            return s.append(") => ").append(ret.name).toString();
        }
        private static String descriptor(Type[] params, Type ret) {
            StringBuilder s = new StringBuilder().append('(');
            for (Type p : params) s.append(p.desc);
            return s.append(')').append(ret.desc).toString();
        }

        public Type getReturnType() { return ret; }
        public Type[] getParams() { return params; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Function type = (Function) o;
            return ret.equals(type.ret) && Arrays.equals(params, type.params);
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder().append("Function Type: (");
            for (Type p : params) s.append(p.name).append(", ");
            if (params.length > 0) { s.deleteCharAt(s.length() - 1); s.deleteCharAt(s.length() - 1); }
            return s.append(" => ").append(ret.name).toString();
        }
    }
}