package jvm.methods;

import jvm.AttributeInfo;
import jvm.Opcodes;
import jvm.classes.ClassFile;
import util.ByteList;
import util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Code extends AttributeInfo implements Opcodes
{
    private ByteList code = new ByteList();
    private List<AttributeInfo> attributes = new ArrayList<>();
    private StackMapTable smt;
    private Map<Frame, List<Pair<Integer, Frame>>> frames = new HashMap<>();

    Code(ClassFile cls, MethodInfo method) {
        super(cls, "Code");
        smt = new StackMapTable(cls, method.getParams());
    }

    public void addAttribute(AttributeInfo attribute) { attributes.add(attribute); }

    public void push(byte opcode, String type) {
        code.addByte(opcode);
        smt.push(type);
    }

    /* Push const from constPool (BYTE INDEX) onto the stack */
    public void push(byte opcode, byte constIndex, String type) {
        code.addByte(opcode);
        code.addByte(constIndex);
        smt.push(type);
    }

    /* Push const from constPool (SHORT INDEX) onto the stack */
    public void push(byte opcode, short constIndex, String type) {
        code.addByte(opcode);
        code.addShort(constIndex);
        smt.push(type);
    }

    /* Pop from stack to local (op takes localIndex as parameter) */
    public void pop(byte opcode, byte localIndex) { pop(opcode, localIndex, true); }

    /* Pop from stack to local */
    public void pop(byte opcode, byte localIndex, boolean insertLocalIndex) {
        String type = smt.pop();
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

    public void invoke(byte opcode, short methodIndex, String type, int parameters, boolean isStatic) {
        code.addByte(opcode);
        code.addShort(methodIndex);
        smt.pop(parameters + (isStatic ? 0 : 1));
        if (!type.equals("V")) smt.push(type);
    }

    public void operator(byte opcode, int operands, String type) {
        code.addByte(opcode);
        if (operands > 0) smt.pop(operands);
        smt.push(type);
    }

    public void retOperator(byte opcode, boolean isVoid) {
        if (!isVoid) smt.pop();
        code.addByte(opcode);
    }

    public void jmpOperator(byte opcode, int operands, Frame target) {
        if (operands > 0) smt.pop(operands);
        Frame here = assignFrame(new Frame(), false);
        code.addByte(opcode);
        frames.putIfAbsent(target, new ArrayList<>());
        frames.get(target).add(Pair.of(code.size(), here));
        code.addShort(0x0000);
    }

    public void chop(int amt) { smt.chop(amt); }

    public Frame assignFrame(Frame frame) { return assignFrame(frame, true); }
    private Frame assignFrame(Frame frame, boolean use) {
        if (frame.getOffset() != -1) throw new RuntimeException(); // fixme-maybe not?
        frame.setOffset(code.size());
        smt.setFrame(frame);
        if (use) smt.useFrame();
        return frame;
    }

    public String getStackHeadType() { return smt.peekStack(); }
    public String getType(int localIndex) { return smt.load(localIndex); }

    @Override
    public ByteList toByteList() {
        if (smt.getSize() != 0) attributes.add(smt);
        for (Frame frame : frames.keySet())
            for (Pair<Integer, Frame> pair : frames.get(frame)) {
                if (pair.getValue() == null ||
                        !frame.getStack().equals(pair.getValue().getStack()))
                    throw new RuntimeException("conditional code alters stack (" + frame.getStack() +  "->" + pair.getValue().getStack() + ')');

                int offset = frame.getOffset() - pair.getKey() + 1;
                code.set(pair.getKey(), (byte) (offset >> 8 & 0xFF));
                code.set(pair.getKey() + 1, (byte) (offset & 0xFF));
            }

        info.addShort(smt.getMaxStack());     // max_stack
        info.addShort(smt.getMaxLocals());    // max_locals
        info.addInt(code.size());             // code_length
        info.addAll(code);                    // code[code_length]
        info.addShort(0x0000);              // exception_table_length
        info.addShort(attributes.size());     // attributes_count
        for (AttributeInfo attrib : attributes)
            info.addAll(attrib.toByteList()); // attributes[attributes_count]

        return super.toByteList();
    }
}
