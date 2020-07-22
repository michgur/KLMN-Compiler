package codegen;

import jvm.JVMType;
import jvm.Opcodes;
import jvm.classes.ClassFile;
import jvm.classes.ConstPool;
import jvm.classes.FieldInfo;
import jvm.methods.Code;
import jvm.methods.Label;
import jvm.methods.MethodInfo;
import lexing.Symbol;
import parsing.AST;
import util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CodeGenerator
{
    private final Map<Production, Visitor> visitors = new HashMap<>();
    private final SymbolTable symbolTable = new SymbolTable();
    private final ClassFile classFile;
    private final ConstPool constPool;
    private final Map<String, Code> methods = new HashMap<>();
    private Code code;

    public CodeGenerator(ClassFile classFile) {
        this.classFile = classFile;
        this.constPool = classFile.getConstPool();
    }

    /* Add a visitor for a specific production */
    public void addVisitor(Symbol symbol, Symbol[] production, Visitor visitor) { visitors.put(new Production(symbol, production), visitor); }
    /* Same visitor for every production of symbol */
    public void addVisitor(Symbol symbol, Visitor visitor)
    { for (Symbol[] production : symbol.getProductions()) visitors.put(new Production(symbol, production), visitor); }

    public void apply(AST ast) {
        StringBuilder message = new StringBuilder("undefined behavior for production " + ast.getValue() + " -> ");
        for (Symbol s : ast.getProduction()) message.append(s).append(" ");
        Visitor visitor = visitors.get(new Production(ast.getValue(), ast.getProduction()));
        if (visitor == null) switch (ast.getProduction().length) {
            case 0:
                break;
            case 1:
                apply(ast.getChildren()[0]);
                break;
            default:
                StringBuilder error = new StringBuilder("undefined behavior for production " + ast.getValue() + " -> ");
                for (Symbol s : ast.getProduction()) error.append(s).append(" ");
                throw new RuntimeException(error.toString());
        }
        else visitor.apply(this, ast.getChildren());
    }

    public SymbolTable getSymbolTable() { return symbolTable; }

    public interface Visitor { void apply(CodeGenerator generator, AST... ast); }

    public ClassFile getClassFile() { return classFile; }

    public void addMethod(String name, int accFlags, JVMType type, JVMType... params) {
        MethodInfo method = new MethodInfo(classFile, name, accFlags, type, params);
        methods.put(name, method.getCode());
        // todo symbolTable call
    }
    public void editMethod(String name) {
        // todo ret for current method
        code = methods.remove(name);
        if (code == null) throw new RuntimeException(name + " is not a method name!");
        symbolTable.resetIndex();
    }
    public void addField(String name, int accFlags, JVMType type) {
        FieldInfo field = new FieldInfo(classFile, name, accFlags, type);
        classFile.getFields().add(field);
        symbolTable.addSymbol(classFile.getName(), name, type, (accFlags & Opcodes.ACC_STATIC) != 0);
    }

    public Label assign(Label label) { return code.assign(label); }

    private static int getOperandsAmount(byte opcode)
    { return Opcodes.binaryOperators.contains(opcode) ? 2 : Opcodes.unaryOperators.contains(opcode) ? 1 : 0; }

    public void pushNull() { code.push(Opcodes.ACONST_NULL, null); }
    public void pushString(String value) { code.push(Opcodes.LDC, (byte) constPool.addString(value), JVMType.refType("java/lang/String")); }
    public void pushInt(int value) {
        if (value <= 5 && value >= -1) code.push((byte) (Opcodes.ICONST_M1 + value + 1), JVMType.INTEGER);
        else code.push(Opcodes.LDC, (byte) constPool.addInteger(value), JVMType.INTEGER);
    }
    public void pushFloat(float f) {
        if (f == 0) code.push(Opcodes.FCONST_0, JVMType.FLOAT);
        else if (f == 1) code.push(Opcodes.FCONST_1, JVMType.FLOAT);
        else if (f == 2) code.push(Opcodes.FCONST_2, JVMType.FLOAT);
        else code.push(Opcodes.LDC, (byte) constPool.addFloat(f), JVMType.FLOAT);
    }
    public void pushNew(JVMType cls) {
        code.push(Opcodes.NEW, constPool.addClass(cls), cls);
        code.push(Opcodes.DUP, cls);
    }
    public JVMType peekType() { return code.peek(); }
    public void init(String cls, JVMType... params) {
        code.invoke(Opcodes.INVOKESPECIAL, constPool.addMethodref(cls, "<init>",
                JVMType.methodDescriptor(JVMType.VOID, params)), JVMType.VOID, params.length, false);
    }
    public void push(String identifier) {
        SymbolTable.SymbolInfo info = symbolTable.findSymbol(identifier);
        JVMType type = info.getType();
        if (info.isLocal()) {
            int index = info.getLocalIndex();
            if (index < 4) code.push(Opcodes.getLoadOpcode(index, type), type);
            else code.push(Opcodes.getLoadOpcode(index, type), (byte) index, type);
        }
        else if (info.isField()) {
            code.push(info.isStaticField() ? Opcodes.GETSTATIC : Opcodes.GETFIELD, 
                    constPool.addFieldref(info.getDeclaringClassName(), identifier, type), type);
        }
    }
    public void pushField(String cls, String name, JVMType type, boolean isStatic) {
        code.push((isStatic) ? Opcodes.GETSTATIC : Opcodes.GETFIELD, constPool.addFieldref(cls, name, type), type);
    }
    public void popTo(String identifier) {
        SymbolTable.SymbolInfo info = symbolTable.findSymbol(identifier);
        JVMType type = info.getType();
        if (info.isLocal()) { 
            int index = info.getLocalIndex();
            code.pop(Opcodes.getStoreOpcode(index, type), type, (byte) index, index > 3); 
        }
        else if (info.isField()) {
            code.pop(info.isStaticField() ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD,
                    constPool.addFieldref(info.getDeclaringClassName(), identifier, type));
        }
    }
    public void call(String cls, String method, JVMType type, JVMType... params) {
        code.invoke(Opcodes.INVOKEVIRTUAL, constPool.addMethodref(cls, method,
                JVMType.methodDescriptor(type, params)), type, params.length, false);
    }
    //    public void call(String cls, String method, Type.Function type) {
//        code.invoke(INVOKEVIRTUAL, constPool.addMethodref(cls, method,
//                type.getDescriptor()), type.getReturnType().getDescriptor(), type.getParams().length, false);
//    }
//    public void callStatic(String cls, String method, Type.Function type) {
//        code.invoke(INVOKESTATIC, constPool.addMethodref(cls, method,
//                type.getDescriptor()), type.getReturnType().getDescriptor(), type.getParams().length, true);
//    }
    public void dup(Type type) { code.push(Opcodes.DUP, type.getJvmType()); }
    public void pushFromArray(String name) {
        JVMType type = symbolTable.typeOf(name);
        code.operator(Opcodes.getArrayLoadOpcode(type), 2, type.getBaseType());
    }
    public void pop() { code.pop(Opcodes.POP); }
    public void popToArray(String name) {
        JVMType type = symbolTable.typeOf(name);
        code.popMultiple(Opcodes.getArrayStoreOpcode(type), 3);
    }
    public void useOperator(byte opcode) { code.operator(opcode, getOperandsAmount(opcode), Opcodes.getType(opcode)); }
    public void useJmpOperator(byte opcode, Label target)
    { code.jmpOperator(opcode, getOperandsAmount(opcode), target); }
    public void convertTop(JVMType from, JVMType to) { code.operator(Opcodes.getConvertOpcode(from, to), 1, to); }
    public void pushNewArray(JVMType type) {
        int arrayType = switch (type.getDescriptor()) {
            case "[Z" -> 4;
            case "[C" -> 5;
            case "[F" -> 6;
            case "[D" -> 7;
            case "[B" -> 8;
            case "[S" -> 9;
            case "[I" -> 10;
            case "[J" -> 11;
            default -> throw new RuntimeException("unsupported array type " + type.getDescriptor());
        };
        code.operator(Opcodes.NEWARRAY, (byte) arrayType, 1, type);
    }
    public void dup() { code.dup(); }

    public void ret() { ret(JVMType.VOID); }
    public void ret(JVMType type) {
        symbolTable.exitScope();
        if (type == JVMType.VOID) code.retOperator(Opcodes.RETURN, true);
        else if (type == JVMType.INTEGER) code.retOperator(Opcodes.IRETURN, false);
        else if (type == JVMType.FLOAT) code.retOperator(Opcodes.FRETURN, false);
        else if (type == JVMType.LONG) code.retOperator(Opcodes.LRETURN, false);
        else if (type == JVMType.DOUBLE) code.retOperator(Opcodes.DRETURN, false);
        else code.retOperator(Opcodes.ARETURN, false);
    }

    private static class Production extends Pair<Symbol, Symbol[]> {
        public Production(Symbol key, Symbol[] value) { super(key, value); }
        @Override
        public int hashCode() { return 0; }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Production)) return false;

            Production pair = (Production) o;
            return (Objects.equals(getKey(), pair.getKey())) && (Arrays.equals(getValue(), pair.getValue()));
        }
    }
}
