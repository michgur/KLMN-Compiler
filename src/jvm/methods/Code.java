package jvm.methods;

import jvm.AttributeInfo;
import jvm.JVMType;
import jvm.Opcodes;
import jvm.classes.ClassFile;
import util.ByteList;

import java.util.*;

public class Code extends AttributeInfo implements Opcodes
{
    private final ByteList code = new ByteList();
    private final List<AttributeInfo> attributes = new ArrayList<>();
    private final StackMapTable smt;
    private final Map<Label, Set<Integer>> frames = new HashMap<>();

    Code(ClassFile cls, MethodInfo method) {
        super(cls, "Code");
        smt = new StackMapTable(cls, method.getParams());
        attributes.add(smt);
    }

    public void addAttribute(AttributeInfo attribute) { attributes.add(attribute); }

    public void push(byte opcode, JVMType type) {
        code.addByte(opcode);
        smt.push(type);
    }

    /* Push const from constPool (BYTE INDEX) onto the stack */
    public void push(byte opcode, byte constIndex, JVMType type) {
        code.addByte(opcode);
        code.addByte(constIndex);
        smt.push(type);
    }

    /* Push const from constPool (SHORT INDEX) onto the stack */
    public void push(byte opcode, short constIndex, JVMType type) {
        code.addByte(opcode);
        code.addShort(constIndex);
        smt.push(type);
    }

    /* Pop from stack to local (op takes localIndex as parameter) */
    public void pop(byte opcode, JVMType type, byte localIndex) { pop(opcode, type, localIndex, true); }

    /* Pop from stack to local */
    public void pop(byte opcode, JVMType type, byte localIndex, boolean insertLocalIndex) {
        smt.pop();
        smt.store(localIndex, type);
        code.addByte(opcode);
        if (insertLocalIndex) code.addByte(localIndex);
    }

    /* Pop from stack to field */
    public void pop(byte opcode, short field) {
        smt.pop();
        code.addByte(opcode);
        code.addShort(field);
    }

    /* Pop from stack */
    public void pop(byte opcode) {
        smt.pop();
        code.addByte(opcode);
    }

    /* Pop multiple items from stack */
    public void popMultiple(byte opcode, int count) {
        if (count > 0) smt.pop(count);
        code.addByte(opcode);
    }

    /* Peek stack top */
    public JVMType peek() { return smt.peek(); }

    /* Duplicate stack top */
    public void dup() {
        code.addByte(DUP);
        smt.push(smt.peek());
    }

    public void invoke(byte opcode, short methodIndex, JVMType type, int parameters, boolean isStatic) {
        code.addByte(opcode);
        code.addShort(methodIndex);
        smt.pop(parameters + (isStatic ? 0 : 1));
        if (!type.equals(JVMType.VOID)) smt.push(type);
    }

    public void operator(byte opcode, int operands, JVMType type) {
        code.addByte(opcode);
        if (operands > 0) smt.pop(operands);
        smt.push(type);
    }

    public void operator(byte opcode, byte info, int operands, JVMType type) {
        code.addByte(opcode);
        code.addByte(info);
        if (operands > 0) smt.pop(operands);
        smt.push(type);
    }

    public void retOperator(byte opcode, boolean isVoid) {
        if (!isVoid) smt.pop();
        code.addByte(opcode);
    }

    public void jmpOperator(byte opcode, int operands, Label target) {
        if (operands > 0) smt.pop(operands);
        int i = code.size();
        code.addByte(opcode);
        frames.putIfAbsent(target, new HashSet<>());
        frames.get(target).add(i + 1);
        code.addShort(0x0000);
        smt.jmp(target, i, opcode == GOTO);
    }

    public void chop(int amt) {}

    public Label assign(Label label) {
        smt.assign(label, code.size());
        return label;
    }

    @Override
    public ByteList toByteList() {
        for (Label frame : frames.keySet())
            for (int pair : frames.get(frame)) {
                int offset = frame.block.getOffset() - pair + 1;
                code.set(pair, (byte) (offset >> 8 & 0xFF));
                code.set(pair + 1, (byte) (offset & 0xFF));
            }

        info.addShort(smt.getMaxStack());       // max_stack
        info.addShort(smt.getMaxLocals());      // max_locals
        info.addInt(code.size());               // code_length
        info.addAll(code);                      // code[code_length]
        info.addShort(0x0000);               // exception_table_length
        info.addShort(attributes.size());       // attributes_count
        for (AttributeInfo attrib : attributes) if (attrib.include())
            info.addAll(attrib.toByteList());   // attributes[attributes_count]

        return super.toByteList();
    }
}
