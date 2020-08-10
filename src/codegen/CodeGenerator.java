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
    /* Map of visitors, each telling the CodeGenerator how to generate bytecode from a specific Symbol production */
    private final Map<Production, Visitor> visitors = new HashMap<>();
    private final Map<Production, TypeCheckedVisitor> typeCheckers = new HashMap<>();
    /* The Symbol Table for this code, which keeps track of names in the
     * code by scope, and maps them to their memory location */
    private final SymbolTable symbolTable = new SymbolTable();
    /* The destination ClassFile */
    private final ClassFile classFile;
    /* The Constant Pool of the ClassFile which stores constants & outside names in the code */
    private final ConstPool constPool;
    /* Map of all the methods of the ClassFile */
    private final Map<String, Code> methods = new HashMap<>();
    /* The code attribute of the current method, the actual bytecode is inserted in it */
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
    /* Add a visitor for a specific production */
    public void addTypeCheckedVisitor(Symbol symbol, Symbol[] production, TypeCheckedVisitor typeChecker)
    { typeCheckers.put(new Production(symbol, production), typeChecker); }

    /* Apply bytecode generation for this AST. Will find the Visitor corresponding for
     * Symbol Production represented by this AST and its children */
    public void apply(AST ast) {
        Visitor visitor = visitors.get(new Production(ast.getValue(), ast.getProduction()));
        TypeCheckedVisitor typeCheckedVisitor = typeCheckers.get(new Production(ast.getValue(), ast.getProduction()));
        if (visitor == null) {
            if (typeCheckedVisitor != null) typeCheckedVisitor.apply(this, ast.getChildren());
            else switch (ast.getProduction().length) {
                // Default operations for when there is no visitor for the production
                case 0: break;
                case 1:
                    apply(ast.getChildren()[0]);
                    break;
                default:
                    StringBuilder error = new StringBuilder("undefined behavior for production " + ast.getValue() + " -> ");
                    for (Symbol s : ast.getProduction()) error.append(s).append(" ");
                    throw new RuntimeException(error.toString());
            }
        }
        else visitor.apply(this, ast.getChildren());
    }

    public JVMType applyTypeChecked(AST ast) {
        TypeCheckedVisitor typeChecker = typeCheckers.get(new Production(ast.getValue(), ast.getProduction()));
        // Default operations for when there is no type checker for the production
        if (typeChecker == null) {
            if (ast.getProduction().length == 1) return applyTypeChecked(ast.getChildren()[0]);
            else {
                StringBuilder error = new StringBuilder("undefined type checked behavior for production " + ast.getValue() + " -> ");
                for (Symbol s : ast.getProduction()) error.append(s).append(" ");
                throw new RuntimeException(error.toString());
            }
        } else return typeChecker.apply(this, ast.getChildren());
    }

    public SymbolTable getSymbolTable() { return symbolTable; }

    /* This single-method interface generates bytecode from an AST representing a Symbol Production */
    public interface Visitor { void apply(CodeGenerator generator, AST... ast); }

    public interface TypeCheckedVisitor { JVMType apply(CodeGenerator generator, AST... ast); }

    public ClassFile getClassFile() { return classFile; }

    public void addMethod(String name, int accFlags, JVMType type, JVMType... params) {
        MethodInfo method = new MethodInfo(classFile, name, accFlags, type, params);
        methods.put(name, method.getCode());
        // todo symbolTable call
    }
    /* Edit the code of this method. Methods cannot be edited in parallel */
    public void editMethod(String name) {
        // todo ret for current method
        code = methods.remove(name);
        if (code == null) throw new RuntimeException(name + " is not a method name!");
        symbolTable.resetIndex();
    }
    /* Add a field for the ClassFile */
    public void addField(String name, int accFlags, JVMType type) {
        FieldInfo field = new FieldInfo(classFile, name, accFlags, type);
        classFile.getFields().add(field);
        symbolTable.addSymbol(classFile.getName(), name, type, (accFlags & Opcodes.ACC_STATIC) != 0);
    }

    /* Assign the Label to this codepoint. JMP instructions pointing to this Label will be edited */
    public void assign(Label label) { code.assign(label); }

    /* Push a null reference onto the stack */
    public void pushNull() { code.push(Opcodes.ACONST_NULL, null); }
    /* Push a constant String onto the stack */
    public void pushString(String value) { code.push(Opcodes.LDC, (byte) constPool.addString(value), JVMType.refType("java/lang/String")); }
    /* Push a constant Integer onto the stack */
    public void pushInt(int value) {
        if (value <= 5 && value >= -1) code.push((byte) (Opcodes.ICONST_M1 + value + 1), JVMType.INTEGER);
        else code.push(Opcodes.LDC, (byte) constPool.addInteger(value), JVMType.INTEGER);
    }
    /* Push a constant Float onto the stack */
    public void pushFloat(float f) {
        if (f == 0) code.push(Opcodes.FCONST_0, JVMType.FLOAT);
        else if (f == 1) code.push(Opcodes.FCONST_1, JVMType.FLOAT);
        else if (f == 2) code.push(Opcodes.FCONST_2, JVMType.FLOAT);
        else code.push(Opcodes.LDC, (byte) constPool.addFloat(f), JVMType.FLOAT);
    }
    /* Push a new instance of cls onto the stack */
    public void pushNew(JVMType cls) {
        code.push(Opcodes.NEW, constPool.addClass(cls), cls);
        code.push(Opcodes.DUP, cls);
    }
    /* Peek the type of the item recently pushed to the stack */
    public JVMType peekType() { return code.peek(); }
    /* Push a variable onto the stack. Will use information from the SymbolTable to determine
     * whether the variable is a local or a Field */
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
    /* Push a Field onto the stack. If the Field is not static this assumes
     * that an instance of cls is on the stack */
    public void pushField(String cls, String name, JVMType type, boolean isStatic) {
        code.push((isStatic) ? Opcodes.GETSTATIC : Opcodes.GETFIELD, constPool.addFieldref(cls, name, type), type);
    }
    /* Pop an item from the stack to a variable. Will use information from the SymbolTable to determine
     * whether the variable is a local or a Field. Assumes the stack is not empty */
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
    /* Call a Method of cls. If the Method is not static this assumes
     * that an instance of cls is on the stack */
    public void call(String cls, String method, JVMType type, JVMType... params) {
        code.invoke(Opcodes.INVOKEVIRTUAL, constPool.addMethodref(cls, method,
                JVMType.methodDescriptor(type, params)), type, params.length, false);
    }
    /* Push an item from an array onto the stack.
     * This assumes the array and an integer index are on the stack */
    public void pushFromArray(String name) {
        JVMType type = symbolTable.typeOf(name);
        code.operator(Opcodes.getArrayLoadOpcode(type), 2, type.getBaseType());
    }
    /* Pop an item from the stack. */
    public void pop() { code.pop(Opcodes.POP); }
    /* Pop an item from the stack and insert it into an array.
     * This assumes the array and an integer index are on the stack */
    public void popToArray(String name) {
        JVMType type = symbolTable.typeOf(name);
        code.popMultiple(Opcodes.getArrayStoreOpcode(type), 3);
    }
    /* Add an operator to the code. This assumes the bytecode doesn't require more information,
     * and the required operands are on the stack */
    public void useOperator(byte opcode) { code.operator(opcode, Opcodes.getOperandsAmount(opcode), Opcodes.getType(opcode)); }
    /* Add a control flow operator to the code. This assumes the bytecode doesn't require more information,
     * and the required operands are on the stack. target must eventually be assigned to a specific codepoint */
    public void useJmpOperator(byte opcode, Label target)
    { code.jmpOperator(opcode, Opcodes.getOperandsAmount(opcode), target); }
    /* Convert the type of the item on top of the stack. Assumes an item of type 'from' is on top of the stack. */
    public void convertTop(JVMType from, JVMType to) { code.operator(Opcodes.getConvertOpcode(from, to), 1, to); }
    /* Push a new Array of primitive type onto the stack.
     * This assumes an integer representing the size of the array is on top of the stack. */
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
    /* Duplicates the item on top of the stack. Assumes the stack isn't empty. */
    public void dup() { code.dup(); }
    /* Return from the current Method */
    public void ret() { ret(JVMType.VOID); }
    /* Return an item of type from the current Method */
    public void ret(JVMType type) {
        if (type == JVMType.VOID) code.retOperator(Opcodes.RETURN, true);
        else if (type == JVMType.INTEGER) code.retOperator(Opcodes.IRETURN, false);
        else if (type == JVMType.FLOAT) code.retOperator(Opcodes.FRETURN, false);
        else if (type == JVMType.LONG) code.retOperator(Opcodes.LRETURN, false);
        else if (type == JVMType.DOUBLE) code.retOperator(Opcodes.DRETURN, false);
        else code.retOperator(Opcodes.ARETURN, false);
    }

    /* Represents a Symbol Production. Overrides equals / hashCode to be used as Map keys  */
    private static class Production extends Pair<Symbol, Symbol[]> {
        public Production(Symbol key, Symbol[] value) { super(key, value); }

        @Override
        public int hashCode() {
            int result = getKey() != null ? getKey().hashCode() : 0;
            result = 31 * result + (getValue() != null ? Arrays.hashCode(getValue()) : 0);
            return result;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Production)) return false;

            Production pair = (Production) o;
            return (Objects.equals(getKey(), pair.getKey())) && (Arrays.equals(getValue(), pair.getValue()));
        }
    }
}
