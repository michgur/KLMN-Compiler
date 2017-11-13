package klmn.writing;

import jvm.Opcodes;
import jvm.classes.ConstPool;
import jvm.methods.Code;
import jvm.methods.Frame;
import jvm.methods.MethodInfo;

public class MethodWriter implements Opcodes
{
    private ConstPool constPool;
    private Code code;

    private SymbolTable st;
    private TypeEnv tm = new TypeEnv();
    // vars for writing conditions
    private Frame condEnd;
    private boolean inCond = false, skipFor = false, returned = false;

    private String parentName;

    public MethodWriter(String parentName, ModuleWriter parent, boolean newScope, ConstPool constPool, MethodInfo method, String... paramNames) {
        this.parentName = parentName;
        this.constPool = constPool;
        code = method.getCode();
        st = parent.getSymbolTable();
        if (newScope) st.enterScope(SymbolTable.ScopeType.FUNCTION);
        String[] paramTypes = method.getParams();
        if (paramNames.length != paramTypes.length)
            throw new RuntimeException("Parameter names array's length doesn't match with method");
        for (int i = 0; i < paramNames.length; i++)
            st.addSymbol(paramNames[i], tm.getForDescriptor(paramTypes[i]));
    }

    public int findSymbol(String symbol) { return st.findSymbol(symbol); }
    public TypeEnv.Type typeOf(String symbol) { return st.typeOf(symbol); }
    public boolean checkScope(String symbol) { return st.checkScope(symbol); }

    public void enterScope() { enterScope(SymbolTable.ScopeType.BLOCK); }
    public void enterScope(SymbolTable.ScopeType type) { st.enterScope(type); }
    public void exitScope() {
        int size = st.exitScope();
        code.chop(size);
    }

    public boolean hasReturned(SymbolTable.ScopeType type) { return st.hasRet(type); }

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
    public void callStatic(String cls, String method, String type, String... params) {
        code.invoke(INVOKESTATIC, constPool.addMethodref(cls, method,
                '(' + String.join("", params) + ')' + type), type, params.length, true);
    }
    public void pushLocal(String name) {
        int index = st.findSymbol(name);
        switch (st.scopeTypeOf(name)) {
            case FUNCTION:
            case BLOCK: // local variable
                switch (code.getType(index)) {
                    case "I":
                        if (index <= 3) code.push((byte) (ILOAD_0 + index), "I");
                        else code.push(ILOAD, (byte) index, "I");
                        break;
                    case "F":
                        if (index <= 3) code.push((byte) (FLOAD_0 + index), "F");
                        else code.push(FLOAD, (byte) index, "F");
                        break;
                } break;
            case MODULE: // field of same module
                String type = st.typeOf(name).getDescriptor();
                pushStaticField(parentName, name, type); // todo: something nicer and more object-oriented (variable class and shit like that)
        }
    }
    public void pop() { code.pop(POP); }
    public void popToLocal(String name) {
        int i = findSymbol(name);
        switch (code.getStackHeadType()) {
            case "I":
                if (i <= 3) code.pop((byte) (ISTORE_0 + i), (byte) i, false);
                else code.pop(ISTORE, (byte) i, true);
                break;
            case "F":
                if (i <= 3) code.pop((byte) (FSTORE_0 + i), (byte) i, false);
                else code.pop(FSTORE, (byte) i, true);
                break;
        }
    }
    public void useOperator(byte opcode) { code.operator(opcode, getOperandsAmount(opcode), Opcodes.getType(opcode)); }
    public void useJmpOperator(byte opcode, Frame target)
    { code.jmpOperator(opcode, getOperandsAmount(opcode), target); }

    public void ret() {
        st.ret();
        String type = code.getStackHeadType();
        if (type == null) code.retOperator(RETURN, true);
        else switch (type) {
            case "I": code.retOperator(IRETURN, false); break;
            case "F": code.retOperator(FRETURN, false); break;
            case "J": code.retOperator(LRETURN, false); break;
            case "D": code.retOperator(DRETURN, false); break;
            // todo: more types
        }
    }

    public Frame assignFrame(Frame frame) { return code.assignFrame(frame); }

    private static int getOperandsAmount(byte opcode)
    { return binaryOperators.contains(opcode) ? 2 : unaryOperators.contains(opcode) ? 1 : 0; }

    public Frame getCondEnd() { return condEnd; }
    public void setCondEnd(Frame condEnd) { this.condEnd = condEnd; }

    public boolean isInCond() { return inCond; }
    public void setInCond(boolean inCond) { this.inCond = inCond; }

    public boolean getSkipFor() { return skipFor; }
    public void setSkipFor(boolean skipFor) { this.skipFor = skipFor; }

    public TypeEnv getTypeEnv() { return tm; }

    public String getParentName() { return parentName; }

    public SymbolTable getSymbolTable() { return st; }
}
