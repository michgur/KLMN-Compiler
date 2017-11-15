package klmn.writing;

import ast.AST;
import jvm.Opcodes;
import klmn.nodes.ExpNode;
import lang.Terminal;
import util.Pair;

import java.util.*;

public class TypeEnv implements Opcodes
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

    { // does not belong here
        Type i = add("int", "I");
        add("long", "J");
        Type f = add("float", "F");
        add("double", "D");
        Type s = add("string", "Ljava/lang/StringBuilder;");
        Type bool = add("boolean", "Z");
        add("stringArr", "[Ljava/lang/String;");
        add("void", "V");
        Type sl = add("<stringL>", "Ljava/lang/String;"); // only for literals

        binaryOps = new HashMap<>();
        assignOps = new HashMap<>();

        // addition for strings appears to be more complicated
        Terminal add = new Terminal("+"), sub = new Terminal("-"),
                mul = new Terminal("*"), div = new Terminal("/");
        putOp(add, i, i, new OpSame(IADD), i);
        putOp(add, f, f, new OpSame(FADD), f);
        putOp(add, i, f, new OpIF(FADD), f);
        putOp(add, f, i, new OpFI(FADD), f);
        putOp(sub, i, i, new OpSame(ISUB), i);
        putOp(sub, f, f, new OpSame(FSUB), f);
        putOp(sub, i, f, new OpIF(FSUB), f);
        putOp(sub, f, i, new OpFI(FSUB), f);
        putOp(mul, i, i, new OpSame(IMUL), i);
        putOp(mul, f, f, new OpSame(FMUL), f);
        putOp(mul, i, f, new OpIF(FMUL), f);
        putOp(mul, f, i, new OpFI(FMUL), f);
        putOp(div, i, i, new OpSame(IDIV), i);
        putOp(div, f, f, new OpSame(FDIV), f);
        putOp(div, i, f, new OpIF(FDIV), f);
        putOp(div, f, i, new OpFI(FDIV), f);
        putOpAssign(i, f, (writer, a, b) -> {
            f2i(writer, b);
            writer.popToVar(a.getValue().getValue());
        });
        putOpAssign(f, i, (writer, a, b) -> {
            i2f(writer, b);
            writer.popToVar(a.getValue().getValue());
        });
        putOpAssign(s, sl, (writer, a, b) -> {
            createString(writer, b);
            writer.popToVar(a.getValue().getValue());
        });
        putStringOp(s);
        putStringOp(sl);
        putStringOp(i);
        putStringOp(f);
        putStringOp(bool);

        // slightly better versions
        putOp(add, sl, s, (writer, a, b) -> {
            b.write(writer);
            writer.pushInt(0);
            a.write(writer);
            writer.call("java/lang/StringBuilder", "insert", "Ljava/lang/StringBuilder;", "I", "Ljava/lang/String;");
        }, s);
        putOp(add, s, sl, (writer, a, b) -> {
            a.write(writer);
            b.write(writer);
            writer.call("java/lang/StringBuilder", "append", "Ljava/lang/StringBuilder;", "Ljava/lang/String;");
        }, s);
    }
    private void putStringOp(Type t) {
        Terminal add = new Terminal("+");
        Type s = getForName("string"), sl = getForName("<stringL>");
        String desc = getStringDesc(t);
        putOp(add, t, sl, (writer, a, b) -> {
            b.write(writer);
            writer.pushInt(0);
            createString(writer, a);
            writer.call("java/lang/StringBuilder", "insert", "Ljava/lang/StringBuilder;", "I", desc);
        }, s);
        putOp(add, sl, t, (writer, a, b) -> {
            createString(writer, a);
            b.write(writer);
            writer.call("java/lang/StringBuilder", "append", "Ljava/lang/StringBuilder;", desc);
        }, s);
        putOp(add, t, s, (writer, a, b) -> {
            b.write(writer);
            writer.pushInt(0);
            a.write(writer);
            writer.call("java/lang/StringBuilder", "insert", "Ljava/lang/StringBuilder;", "I", desc);
        }, s);
        putOp(add, s, t, (writer, a, b) -> {
            a.write(writer);
            b.write(writer);
            writer.call("java/lang/StringBuilder", "append", "Ljava/lang/StringBuilder;", desc);
        }, s);
    }

    private static String getStringDesc(Type t) {
        switch (t.getName()) {
            case "int":
            case "float":
            case "boolean":
            case "<stringL>":
                return t.getDescriptor();
            default: return "Ljava/lang/Object;";
        }
    }

    private static class OpSame implements BinaryOperator{
        private byte op; public OpSame(byte op) { this.op = op; }
        @Override public void op(MethodWriter w, ExpNode a, ExpNode b) { a.write(w); b.write(w); w.useOperator(op); }
    }
    private static class OpFI implements BinaryOperator{
        private byte op; public OpFI(byte op) { this.op = op; }
        @Override public void op(MethodWriter w, ExpNode a, ExpNode b) { a.write(w); i2f(w, b); w.useOperator(op); }
    }
    private static class OpIF implements BinaryOperator{
        private byte op; public OpIF(byte op) { this.op = op; }
        @Override public void op(MethodWriter w, ExpNode a, ExpNode b) { i2f(w, a); b.write(w); w.useOperator(op); }
    }
    private static void i2f(MethodWriter writer, ExpNode i) {
        if (!i.getValue().getType().getName().equals("num")) {
            i.write(writer);
            writer.useOperator(I2F);
        } else writer.pushFloat(Float.parseFloat(i.getValue().getValue()));
    }
    private static void f2i(MethodWriter writer, ExpNode f) {
        if (!f.getValue().getType().getName().equals("num")) {
            f.write(writer);
            writer.useOperator(F2I);
        } else writer.pushInt((int) Float.parseFloat(f.getValue().getValue()));
    }
    private static void createString(MethodWriter writer, ExpNode literal) {
        writer.pushNew("java/lang/StringBuilder");
        literal.write(writer);
        writer.init("java/lang/StringBuilder", "Ljava/lang/String;");
    }

    private Map<Terminal, Map<Pair<Type, Type>, Pair<BinaryOperator, Type>>> binaryOps;
    private Map<Pair<Type, Type>, AssignOperator> assignOps;

    public interface AssignOperator { void op(MethodWriter writer, AST a, ExpNode b); }
    public interface BinaryOperator { void op(MethodWriter writer, ExpNode a, ExpNode b); }
    // todo: something scope-based (for op overloading)
    // todo: exception for undefined operators
    public void putOp(Terminal op, Type a, Type b, BinaryOperator code, Type res) {
        binaryOps.putIfAbsent(op, new HashMap<>());
        binaryOps.get(op).put(Pair.of(a, b), Pair.of(code, res));
    }
    public void putOpAssign(Type a, Type b, AssignOperator op) { assignOps.put(Pair.of(a, b), op); }

    public void binaryOp(MethodWriter writer, Terminal op, ExpNode a, ExpNode b) {
        Type ta = a.getType(writer), tb = b.getType(writer);
        BinaryOperator code = binaryOps.get(op).get(Pair.of(ta, tb)).getKey();
        if (code == null)
            throw new RuntimeException("no " + op + " operator defined for types " + ta.getName() + ", " + tb.getName());
        code.op(writer, a, b);
    }
    public Type binaryOpType(Terminal op, Type a, Type b) {
        Type t = binaryOps.get(op).get(Pair.of(a, b)).getValue();
        if (t == null)
            throw new RuntimeException("no " + op + " operator defined for types " + a.getName() + ", " + b.getName());
        return t;
    }
    public void assignOp(MethodWriter writer, AST a, ExpNode b) {
        Type ta = writer.typeOf(a.getValue().getValue()), tb = b.getType(writer);
        if (assignOps.get(Pair.of(ta, tb)) == null) {
            if (! ta.equals(tb))
                throw new RuntimeException("no = operator defined for types " + ta.getName() + ", " + tb.getName());
            b.write(writer);
            writer.popToVar(a.getValue().getValue());
        } else assignOps.get(Pair.of(ta, tb)).op(writer, a, b);
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
