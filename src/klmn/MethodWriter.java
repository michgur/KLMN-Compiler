package klmn;

import jvm.Opcodes;
import jvm.classes.ConstPool;
import jvm.methods.Code;
import jvm.methods.Frame;
import jvm.methods.MethodInfo;

import java.util.*;

public class MethodWriter implements Opcodes
{
    private ConstPool constPool;
    private Code code;
    // TODO: SymbolTable should keep track of types (?)
    private Deque<Map<String, Integer>> symbolTable = new ArrayDeque<>();
    // vars for writing conditions
    private Frame condEnd;
    private boolean inCond, skipFor;

    private int index;

    public MethodWriter(ConstPool constPool, MethodInfo method) {
        this.constPool = constPool;
        code = method.getCode();
        index = method.getParams().length;
        enterScope();
    }

    public void addSymbol(String symbol) { symbolTable.peek().put(symbol, index++); }
    public Integer findSymbol(String symbol) {
        for (Map<String, Integer> scope : symbolTable)
            if (scope.containsKey(symbol)) return scope.get(symbol);
        throw new RuntimeException("symbol " + symbol + " not defined!");
    }
    public boolean checkScope(String symbol) { return symbolTable.peek().containsKey(symbol); }

    public void enterScope() { symbolTable.push(new HashMap<>()); }
    public void exitScope() {
        int size = symbolTable.pop().size();
        index -= size;
        code.chop(size);
    }

    public void pushString(String value) { code.push(LDC, constPool.addString(value), "Ljava/lang/String;"); }
    public void pushInt(int value) {
        if (value <= 5 && value >= -1) code.push((byte) (ICONST_M1 + value + 1), "I");
        code.push(LDC, (byte) constPool.addInteger(value), "I");
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
    public void call(String cls, String method, String type, String... params) {
        code.invoke(INVOKEVIRTUAL, constPool.addMethodref(cls, method,
            '(' + String.join("", params) + ')' + type), type, params.length);
    }
    public void callStatic(String cls, String method, String type, String... params) {
        code.invoke(INVOKESTATIC, constPool.addMethodref(cls, method,
                '(' + String.join("", params) + ')' + type), type, params.length);
    }
    public void pushLocal(String name) {
        int i = findSymbol(name);
        switch (code.getType(i)) {
            case "I":
                if (i <= 3) code.push((byte) (ILOAD_0 + i), "I");
                else code.push(ILOAD, (byte) i, "I");
                break;
            case "F":
                if (i <= 3) code.push((byte) (FLOAD_0 + i), "F");
                else code.push(FLOAD, (byte) i, "F");
                break;
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

    public Frame assignFrame(Frame frame) { return code.assignFrame(frame); }

    private static int getOperandsAmount(byte opcode)
    { return binaryOperators.contains(opcode) ? 2 : unaryOperators.contains(opcode) ? 1 : 0; }

    public Frame getCondEnd() {
        return condEnd;
    }

    public void setCondEnd(Frame condEnd) {
        this.condEnd = condEnd;
    }

    public boolean isInCond() {
        return inCond;
    }

    public void setInCond(boolean inCond) {
        this.inCond = inCond;
    }

    public boolean getSkipFor() {
        return skipFor;
    }

    public void setSkipFor(boolean skipFor) {
        this.skipFor = skipFor;
    }
}
