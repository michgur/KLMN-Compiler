package klmn.writing;

import ast.AST;
import jvm.Opcodes;
import jvm.classes.ConstPool;
import jvm.methods.Code;
import jvm.methods.Label;
import jvm.methods.MethodInfo;
import klmn.nodes.ExpNode;
import klmn.writing.types.Type;
import klmn.writing.types.TypeEnv;

public class MethodWriter implements Opcodes
{
    private ConstPool constPool;
    private Code code;

    private SymbolTable st;
    private TypeEnv te;
    // vars for writing conditions
    private Label condEnd;
    private boolean inCond = false, skipFor = false;

    private String parentName;

    public MethodWriter(String parentName, ModuleWriter parent, boolean newScope, ConstPool constPool, MethodInfo method, String... paramNames) {
        this.parentName = parentName;
        this.constPool = constPool;
        code = method.getCode();
        st = parent.getSymbolTable();
        te = parent.getTypeEnv();
        if (newScope) st.enterScope(SymbolTable.ScopeType.FUNCTION);
        String[] paramTypes = method.getParams();
        if (paramNames.length != paramTypes.length)
            throw new RuntimeException("Parameter names array's length doesn't match with method");
        for (int i = 0; i < paramNames.length; i++)
            st.addSymbol(paramNames[i], te.getForDescriptor(paramTypes[i]));
    }

    public int findSymbol(String symbol) { return st.findSymbol(symbol); }
    public Type typeOf(String symbol) { return st.typeOf(symbol); }
    public boolean checkScope(String symbol) { return st.checkScope(symbol); }

    public void enterScope() { enterScope(SymbolTable.ScopeType.BLOCK); }
    public void enterScope(SymbolTable.ScopeType type) { st.enterScope(type); }
    public void exitScope() {
        int size = st.exitScope();
        code.chop(size);
    }

    /* has the CURRENT SCOPE returned. todo- something clearer
    * (I depend here on the SymbolTable since currently it's the only scope-dependent class) */
    public boolean hasReturned() { return st.hasRet(); }

    public void pushNull() { code.push(ACONST_NULL, null); }
    public void pushString(String value) { code.push(LDC, (byte) constPool.addString(value), "Ljava/lang/String;"); }
    public void pushInt(int value) {
        if (value <= 5 && value >= -1) code.push((byte) (ICONST_M1 + value + 1), "I");
        else code.push(LDC, (byte) constPool.addInteger(value), "I");
    }
    public void pushFloat(float f) {
        if (f == 0) code.push(FCONST_0, "F");
        else if (f == 1) code.push(FCONST_1, "F");
        else if (f == 2) code.push(FCONST_2, "F");
        else code.push(LDC, (byte) constPool.addFloat(f), "F");
    }
    public void pushNew(String cls) {
        code.push(NEW, constPool.addClass(cls), cls);
        code.push(DUP, cls);
    }
    public void init(String cls, String... params) {
        code.invoke(INVOKESPECIAL, constPool.addMethodref(cls, "<init>",
                '(' + String.join("", params) + ")V"), "V", params.length, false);
    }
    public void pushField(String cls, String field, String type)
    { code.push(GETFIELD, constPool.addFieldref(cls, field, type), type); }
    public void pushStaticField(String cls, String field, String type) // TODO: convert to JVM types by yourself
    { code.push(GETSTATIC, constPool.addFieldref(cls, field, type), type); }
    public void popToField(String cls, String field, String type)
    { code.pop(PUTFIELD, constPool.addFieldref(cls, field, type)); }
    public void popToStaticField(String cls, String field, String type)
    { code.pop(PUTSTATIC, constPool.addFieldref(cls, field, type)); }
    public void call(String cls, String method, String type, String... params) {
        code.invoke(INVOKEVIRTUAL, constPool.addMethodref(cls, method,
            '(' + String.join("", params) + ')' + type), type, params.length, false);
    }
    public void call(String cls, String method, Type.Function type) {
        code.invoke(INVOKEVIRTUAL, constPool.addMethodref(cls, method,
                type.getDescriptor()), type.getReturnType().getDescriptor(), type.getParams().length, false);
    }
    public void callStatic(String cls, String method, Type.Function type) {
        code.invoke(INVOKESTATIC, constPool.addMethodref(cls, method,
                type.getDescriptor()), type.getReturnType().getDescriptor(), type.getParams().length, true);
    }
    public void dup(Type type) { code.push(DUP, type.getDescriptor()); }
    public void pushVar(String name) {
        if (st.typeOf(name) instanceof Type.Tuple) {
            for (int i = ((Type.Tuple) st.typeOf(name)).getTypes().length - 1; i >= 0; i--)
                pushVar(name + "#" + i);
            return;
        }
        int index = st.findSymbol(name);
        String desc = st.typeOf(name).getDescriptor();
        switch (st.scopeTypeOf(name)) {
            case FUNCTION:
            case BLOCK: // local variable
                switch (desc) {
                    case "I":
                        if (index <= 3) code.push((byte) (ILOAD_0 + index), "I");
                        else code.push(ILOAD, (byte) index, "I");
                        break;
                    case "F":
                        if (index <= 3) code.push((byte) (FLOAD_0 + index), "F");
                        else code.push(FLOAD, (byte) index, "F");
                        break;
                    default:
                        if (index <= 3) code.push((byte) (ALOAD_0 + index), desc);
                        else code.push(ALOAD, (byte) index, desc);
                } break;
            case MODULE: // field of same module
                pushStaticField(parentName, name, desc); // todo: something nicer and more object-oriented (variable class and shit like that)
        }
    }
    public void pop() { code.pop(POP); }
    public void popToVar(String name) {
        if (st.typeOf(name) instanceof Type.Tuple) {
            for (int i = ((Type.Tuple) st.typeOf(name)).getTypes().length - 1; i >= 0; i--)
                popToVar(name + "#" + i);
            return;
        }
        int i = findSymbol(name);
        String desc = st.typeOf(name).getDescriptor();
        switch (st.scopeTypeOf(name)) {
            case FUNCTION:
            case BLOCK:
                switch (desc) {
                    case "I":
                        if (i <= 3) code.pop((byte) (ISTORE_0 + i), "I", (byte) i, false);
                        else code.pop(ISTORE, "I", (byte) i, true);
                        break;
                    case "F":
                        if (i <= 3) code.pop((byte) (FSTORE_0 + i), "F", (byte) i, false);
                        else code.pop(FSTORE, "F", (byte) i, true);
                        break;
                    default:
                        if (i <= 3) code.pop((byte) (ASTORE_0 + i), desc, (byte) i, false);
                        else code.pop(ASTORE, desc, (byte) i, true);
                } break;
            case MODULE:
                popToStaticField(parentName, name, st.typeOf(name).getDescriptor());
        }
    }
    public void useOperator(byte opcode) { code.operator(opcode, getOperandsAmount(opcode), Opcodes.getType(opcode)); }
    public void useJmpOperator(byte opcode, Label target)
    { code.jmpOperator(opcode, getOperandsAmount(opcode), target); }

    private void createTupleString(ExpNode exp, Type.Tuple type) {
        pushNew("java/lang/StringBuilder");
        init("java/lang/StringBuilder");
        code.push(BIPUSH, (byte) ('(' & 0xFF), "C");
        call("java/lang/StringBuilder", "append", "Ljava/lang/StringBuilder;", "C");
        for (int i = 0; i < type.getTypes().length; i++) {
            if (type.getTypes()[i] instanceof Type.Tuple) {
                createTupleString((ExpNode) exp.getChild(i), (Type.Tuple) type.getTypes()[i]);
                call("java/lang/StringBuilder", "append", "Ljava/lang/StringBuilder;", "Ljava/lang/String;");
            } else {
                ((ExpNode) exp.getChild(i)).write(this);
                String t = type.getTypes()[i].getDescriptor();
                call("java/lang/StringBuilder", "append", "Ljava/lang/StringBuilder;", (t.startsWith("L")) ? "Ljava/lang/Object;" : t);
            }
            if (i != type.getTypes().length - 1) {
                pushString(", ");
                call("java/lang/StringBuilder", "append", "Ljava/lang/StringBuilder;", "Ljava/lang/String;");
            } else {
                code.push(BIPUSH, (byte) (')' & 0xFF), "C");
                call("java/lang/StringBuilder", "append", "Ljava/lang/StringBuilder;", "C");
            }
        }
        call("java/lang/StringBuilder", "toString", "Ljava/lang/String;");
    }
    public void print(ExpNode exp) {
        pushStaticField("java/lang/System", "out", "Ljava/io/PrintStream;");
        if (exp.getType(this) instanceof Type.Tuple) {
            createTupleString(exp, (Type.Tuple) exp.getType(this));
            call("java/io/PrintStream", "print", "V", "Ljava/lang/String;");
            pushStaticField("java/lang/System", "out", "Ljava/io/PrintStream;");
            pushString(" ");
            call("java/io/PrintStream", "print", "V", "Ljava/lang/String;");
            return;
        }
        exp.write(this);
        String type = exp.getType(this).getDescriptor();
        switch (type) {
            case "V": throw new RuntimeException("cannot print void!");
            case "I":
            case "Z":
            case "F":
            case "Ljava/lang/String;":
                call("java/io/PrintStream", "print", "V", type);
                break;
            default:
                call("java/io/PrintStream", "print", "V", "Ljava/lang/Object;");
        }
        pushStaticField("java/lang/System", "out", "Ljava/io/PrintStream;");
        pushString(" ");
        call("java/io/PrintStream", "print", "V", "Ljava/lang/String;");
    }
    public void println() {
        pushStaticField("java/lang/System", "out", "Ljava/io/PrintStream;");
        call("java/io/PrintStream", "print", "V");
    }
    public void println(ExpNode exp) {
        pushStaticField("java/lang/System", "out", "Ljava/io/PrintStream;");
        if (exp.getType(this) instanceof Type.Tuple) {
            createTupleString(exp, (Type.Tuple) exp.getType(this));
            call("java/io/PrintStream", "println", "V", "Ljava/lang/String;");
            return;
        }
        exp.write(this);
        String type = exp.getType(this).getDescriptor();
        switch (type) {
            case "V": throw new RuntimeException("cannot print void!");
            case "I":
            case "F":
            case "Z":
            case "Ljava/lang/String;":
                call("java/io/PrintStream", "println", "V", type);
                break;
            default:
                call("java/io/PrintStream", "println", "V", "Ljava/lang/Object;");
        }
    }

    public void ret() { ret("V"); }
    public void ret(String type) {
        st.ret();
        switch (type) {
            case "V": code.retOperator(RETURN, true); break;
            case "I": code.retOperator(IRETURN, false); break;
            case "F": code.retOperator(FRETURN, false); break;
            case "J": code.retOperator(LRETURN, false); break;
            case "D": code.retOperator(DRETURN, false); break;
            default: code.retOperator(ARETURN, false);
        }
    }

//    public Frame assignFrame(Frame frame) { return code.assignFrame(frame); }
    public Label assign(Label label) { return code.assign(label); }

    private static int getOperandsAmount(byte opcode)
    { return binaryOperators.contains(opcode) ? 2 : unaryOperators.contains(opcode) ? 1 : 0; }

    public Label getCondEnd() { return condEnd; }
    public void setCondEnd(Label condEnd) { this.condEnd = condEnd; }

    public boolean isInCond() { return inCond; }
    public void setInCond(boolean inCond) { this.inCond = inCond; }

    public boolean getSkipFor() { return skipFor; }
    public void setSkipFor(boolean skipFor) { this.skipFor = skipFor; }

    public TypeEnv getTypeEnv() { return te; }

    public String getParentName() { return parentName; }

    public SymbolTable getSymbolTable() { return st; }
}
