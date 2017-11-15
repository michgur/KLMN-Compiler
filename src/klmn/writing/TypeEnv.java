package klmn.writing;

import ast.AST;
import jvm.Opcodes;
import klmn.nodes.ExpNode;
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
        add("string", "Ljava/lang/String;");
        add("boolean", "Z");
        add("shit", "[Ljava/lang/String;");
        add("void", "V");

        addOps = new HashMap<>();
        subOps = new HashMap<>();
        mulOps = new HashMap<>();
        divOps = new HashMap<>();
        assignOps = new HashMap<>();

        putOpAdd(i, i, new OpSame(IADD), i);
        putOpAdd(f, f, new OpSame(FADD), f);
        putOpAdd(i, f, new OpIF(FADD), f);
        putOpAdd(f, i, new OpFI(FADD), f);
        putOpSub(i, i, new OpSame(ISUB), i);
        putOpSub(f, f, new OpSame(FSUB), f);
        putOpSub(i, f, new OpIF(FSUB), f);
        putOpSub(f, i, new OpFI(FSUB), f);
        putOpMul(i, i, new OpSame(IMUL), i);
        putOpMul(f, f, new OpSame(FMUL), f);
        putOpMul(i, f, new OpIF(FMUL), f);
        putOpMul(f, i, new OpFI(FMUL), f);
        putOpDiv(i, i, new OpSame(IDIV), i);
        putOpDiv(f, f, new OpSame(FDIV), f);
        putOpDiv(i, f, new OpIF(FDIV), f);
        putOpDiv(f, i, new OpFI(FDIV), f);
        putOpAssign(i, i, (writer, a, b) -> {
            b.write(writer);
            writer.popToVar(a.getValue().getValue());
        });
        putOpAssign(f, f, (writer, a, b) -> {
            b.write(writer);
            writer.popToVar(a.getValue().getValue());
        });
        putOpAssign(i, f, (writer, a, b) -> {
            f2i(writer, b);
            writer.popToVar(a.getValue().getValue());
        });
        putOpAssign(f, i, (writer, a, b) -> {
            i2f(writer, b);
            writer.popToVar(a.getValue().getValue());
        });
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

    private Map<Pair<Type, Type>, Pair<BinaryOperator, Type>> addOps, subOps, mulOps, divOps;
    private Map<Pair<Type, Type>, AssignOperator> assignOps;

    public interface AssignOperator { void op(MethodWriter writer, AST a, ExpNode b); }
    public interface BinaryOperator { void op(MethodWriter writer, ExpNode a, ExpNode b); }
    // todo: something scope-based (for op overloading)
    public void putOpAdd(Type a, Type b, BinaryOperator op, Type res) { addOps.put(Pair.of(a, b), Pair.of(op, res)); }
    public void putOpSub(Type a, Type b, BinaryOperator op, Type res) { subOps.put(Pair.of(a, b), Pair.of(op, res)); }
    public void putOpMul(Type a, Type b, BinaryOperator op, Type res) { mulOps.put(Pair.of(a, b), Pair.of(op, res)); }
    public void putOpDiv(Type a, Type b, BinaryOperator op, Type res) { divOps.put(Pair.of(a, b), Pair.of(op, res)); }
    public void putOpAssign(Type a, Type b, AssignOperator op) { assignOps.put(Pair.of(a, b), op); }

    public void opAdd(MethodWriter writer, ExpNode a, ExpNode b)
    { addOps.get(Pair.of(a.getType(writer), b.getType(writer))).getKey().op(writer, a, b); }
    public Type opAddType(Type a, Type b) { return addOps.get(Pair.of(a, b)).getValue(); }
    public void opSub(MethodWriter writer, ExpNode a, ExpNode b)
    { subOps.get(Pair.of(a.getType(writer), b.getType(writer))).getKey().op(writer, a, b); }
    public Type opSubType(Type a, Type b) { return subOps.get(Pair.of(a, b)).getValue(); }
    public void opMul(MethodWriter writer, ExpNode a, ExpNode b)
    { mulOps.get(Pair.of(a.getType(writer), b.getType(writer))).getKey().op(writer, a, b); }
    public Type opMulType(Type a, Type b) { return mulOps.get(Pair.of(a, b)).getValue(); }
    public void opDiv(MethodWriter writer, ExpNode a, ExpNode b)
    { divOps.get(Pair.of(a.getType(writer), b.getType(writer))).getKey().op(writer, a, b); }
    public Type opDivType(Type a, Type b) { return divOps.get(Pair.of(a, b)).getValue(); }
    public void opAssign(MethodWriter writer, AST a, ExpNode b)
    { assignOps.get(Pair.of(writer.typeOf(a.getValue().getValue()), b.getType(writer))).op(writer, a, b); }

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
