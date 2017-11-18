package klmn.writing;

import ast.AST;
import jvm.Opcodes;
import jvm.methods.Label;
import klmn.nodes.BoolExpNode;
import klmn.nodes.ExpNode;
import klmn.nodes.NumberLiteral;
import lang.Terminal;
import util.Pair;

import java.util.*;

import static klmn.KGrammar.*;

public class TypeEnv implements Opcodes
{
    private Set<Type> types = new HashSet<>();

    public Type add(String name, String descriptor) { // todo- arrays
        Type t = new Type(name, descriptor);
        types.add(t);
        return t;
    }

    public Type getForName(String name) { return getForName(name, 0); }
    public Type getForName(String name, int dim) {
        for (Type t : types) {
            if (!t.name.equals(name) || t.dim > dim) continue;
            if (t.dim == dim) return t;
            Type a = new Type(t.name,
                    String.join("", Collections.nCopies(dim - t.dim, "[")) + t.desc, dim);
            types.add(a);
            return a;
        }
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
        add("void", "V");
        Type sl = add("<stringL>", "Ljava/lang/String;"); // only for literals
        Type sla = add("<stringL[]>", "[Ljava/lang/String;"); // only for main args

        binaryOps = new HashMap<>();
        binaryCondOps = new HashMap<>();
        assignOps = new HashMap<>();
        
        putOp(plus, i, i, new OpSame(IADD), i);
        putOp(plus, f, f, new OpSame(FADD), f);
        putOp(plus, i, f, new OpIF(FADD), f);
        putOp(plus, f, i, new OpFI(FADD), f);
        putOp(minus, i, i, new OpSame(ISUB), i);
        putOp(minus, f, f, new OpSame(FSUB), f);
        putOp(minus, i, f, new OpIF(FSUB), f);
        putOp(minus, f, i, new OpFI(FSUB), f);
        putOp(times, i, i, new OpSame(IMUL), i);
        putOp(times, f, f, new OpSame(FMUL), f);
        putOp(times, i, f, new OpIF(FMUL), f);
        putOp(times, f, i, new OpFI(FMUL), f);
        putOp(divide, i, i, new OpSame(IDIV), i);
        putOp(divide, f, f, new OpSame(FDIV), f);
        putOp(divide, i, f, new OpIF(FDIV), f);
        putOp(divide, f, i, new OpFI(FDIV), f);
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
        putOp(plus, sl, s, (writer, a, b) -> {
            b.write(writer);
            writer.pushInt(0);
            a.write(writer);
            writer.call("java/lang/StringBuilder", "insert", "Ljava/lang/StringBuilder;", "I", "Ljava/lang/String;");
        }, s);
        putOp(plus, s, sl, (writer, a, b) -> {
            a.write(writer);
            b.write(writer);
            writer.call("java/lang/StringBuilder", "append", "Ljava/lang/StringBuilder;", "Ljava/lang/String;");
        }, s);

        putBoolOp(eq, i, i, (writer, a, b) -> {
            a.write(writer);
            b.write(writer);
            Label tr = new Label(), end = new Label();
            writer.useJmpOperator(IF_ICMPNE, tr);
            writer.pushInt(1);
            writer.useJmpOperator(GOTO, end);
            writer.assign(tr);
            writer.pushInt(0);
            writer.assign(end);
        }, (writer, a, b) -> {
            a.write(writer);
            b.write(writer);
            if (writer.getSkipFor()) writer.useJmpOperator(IF_ICMPEQ, writer.getCondEnd());
            else writer.useJmpOperator(IF_ICMPNE, writer.getCondEnd());
        });
        putBoolOp(eq, f, f, (writer, a, b) -> {
            a.write(writer);
            b.write(writer);
            Label tr = new Label(), end = new Label();
            writer.useOperator(FCMPG);
            writer.useJmpOperator(IFEQ, tr);
            writer.pushInt(0);
            writer.useJmpOperator(GOTO, end);
            writer.assign(tr);
            writer.pushInt(1);
            writer.assign(end);
        }, (writer, a, b) -> {
            a.write(writer);
            b.write(writer);
            writer.useOperator(FCMPG);
            if (writer.getSkipFor()) writer.useJmpOperator(IFEQ, writer.getCondEnd());
            else writer.useJmpOperator(IFNE, writer.getCondEnd());
        });
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
    private Map<Terminal, Map<Pair<Type, Type>, BinaryOperator>> binaryCondOps;
    private Map<Pair<Type, Type>, AssignOperator> assignOps;

    public interface AssignOperator { void op(MethodWriter writer, AST a, ExpNode b); }
    public interface BinaryOperator { void op(MethodWriter writer, ExpNode a, ExpNode b); }
    // todo: something scope-based (for op overloading)
    // todo: exception for undefined operators
    private void putBoolOp(Terminal op, Type a, Type b, BinaryOperator exp, BinaryOperator cond) {
        binaryOps.putIfAbsent(op, new HashMap<>());
        binaryOps.get(op).put(Pair.of(a, b), Pair.of(exp, getForDescriptor("Z")));
        binaryCondOps.putIfAbsent(op, new HashMap<>());
        binaryCondOps.get(op).put(Pair.of(a, b), cond);
    }
    private void putOp(Terminal op, Type a, Type b, BinaryOperator code, Type res) {
        binaryOps.putIfAbsent(op, new HashMap<>());
        binaryOps.get(op).put(Pair.of(a, b), Pair.of(code, res));
    }
    private void putOpAssign(Type a, Type b, AssignOperator op) { assignOps.put(Pair.of(a, b), op); }

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
    public void binaryBoolOp(MethodWriter writer, Terminal op, ExpNode a, ExpNode b) {
        Type ta = a.getType(writer), tb = b.getType(writer);
        BinaryOperator code;
        if (writer.isInCond()) code = binaryCondOps.get(op).get(Pair.of(ta, tb));
        else code = binaryOps.get(op).get(Pair.of(ta, tb)).getKey();
        if (code == null)
            throw new RuntimeException("no " + op + " operator defined for types " + ta.getName() + ", " + tb.getName());
        code.op(writer, a, b);
    }

    public static class Type
    {
        private String desc, name;
        private int dim; // -ensions
        private Type(String name, String desc) { this(name, desc, 0); }
        private Type(String name, String desc, int dim) {
            this.name = name;
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
    }
}
