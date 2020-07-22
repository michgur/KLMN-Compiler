package codegen;

import jvm.JVMType;

public class Type
{
    /* The name of this Type in the language */
    private String name;
    /* The JVM Type corresponding to this Type */
    private JVMType jvmType;
    protected int dim = 0;

    public Type(String name) { this(name, JVMType.refType(name)); }
    public Type(String name, JVMType jvmType) {
        this.name = name;
        this.jvmType = jvmType;
    }

    public String getName() { return name; }
    public JVMType getJvmType() { return jvmType; }

    public boolean isArray() { return dim > 0; }
    public int getDimensions() { return dim; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Type type = (Type) o;
        return dim == type.dim && jvmType.equals(type.jvmType) && name.equals(type.name);
    }

    @Override
    public String toString() { return "Type " + name + " (descriptor: " + jvmType.getDescriptor() + ')'; }

    public static class Array extends Type
    {
        public Array(Type base) {
            super(base.name, JVMType.arrayType(base.jvmType, 1));
            dim = base.dim + 1;
        }
    }
//
//    public static class Tuple extends Type
//    {
//        private Type[] types;
//        public Tuple(Type... types) {
//            super(name(types), "tupie poopy");
//            this.types = types;
//        }
//
//        private static String name(Type... types) {
//            StringBuilder s = new StringBuilder().append('(');
//            for (Type p : types) s.append(p.name).append(", ");
//            return s.deleteCharAt(s.length() - 1).deleteCharAt(s.length() - 1).append(')').toString();
//        }
//
//        public Type[] getTypes() { return types; }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//
//            Tuple type = (Tuple) o;
//            return Arrays.equals(types, type.types);
//        }
//    }
//
//    public static class Function extends Type
//    {
//        private Type ret;
//        private Type[] params;
//        public Function(Type ret, Type... params) {
//            super(name(params, ret), descriptor(params, ret));
//            this.ret = ret;
//            this.params = params;
//        }
//
//        private static String name(Type[] params, Type ret) {
//            StringBuilder s = new StringBuilder().append('(');
//            for (Type p : params) s.append(p.name).append(", ");
//            if (params.length > 0) s.deleteCharAt(s.length() - 1).deleteCharAt(s.length() - 1);
//            return s.append(") => ").append(ret.name).toString();
//        }
//        private static String descriptor(Type[] params, Type ret) {
//            StringBuilder s = new StringBuilder().append('(');
//            for (Type p : params) s.append(p.desc);
//            return s.append(')').append(ret.desc).toString();
//        }
//
//        public Type getReturnType() { return ret; }
//        public Type[] getParams() { return params; }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//
//            Function type = (Function) o;
//            return ret.equals(type.ret) && Arrays.equals(params, type.params);
//        }
//
//        @Override
//        public String toString() {
//            StringBuilder s = new StringBuilder().append("Function Type: (");
//            for (Type p : params) s.append(p.name).append(", ");
//            if (params.length > 0) { s.deleteCharAt(s.length() - 1); s.deleteCharAt(s.length() - 1); }
//            return s.append(" => ").append(ret.name).toString();
//        }
//    }
}