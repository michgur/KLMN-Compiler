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

    public void pushString(String s) {
        code.addByte(Opcodes.LDC);
        code.addByte(cls.getConstPool().addString(s));
        smt.push("Ljava/lang/String;");
    }

    public void pushField(byte opcode, short fieldIndex, String type) {
        code.addByte(opcode);
        code.addShort(fieldIndex);
        smt.push(type);
    }

    public void invoke(byte opcode, short methodIndex, String type, int parameters) {
        code.addByte(opcode);
        code.addShort(methodIndex);
        smt.pop(parameters + 1);
        if (!type.equals("V")) smt.push(type);
    }

    public void pushInt(int i) {
        smt.push("I");
        switch (i) {
            case-1: code.addByte(Opcodes.ICONST_M1); return;
            case 0: code.addByte(Opcodes.ICONST_0); return;
            case 1: code.addByte(Opcodes.ICONST_1); return;
            case 2: code.addByte(Opcodes.ICONST_2); return;
            case 3: code.addByte(Opcodes.ICONST_3); return;
            case 4: code.addByte(Opcodes.ICONST_4); return;
            case 5: code.addByte(Opcodes.ICONST_5); return;
            default:
                code.addByte(Opcodes.LDC);
                code.addByte(cls.getConstPool().addInteger(i));
        }
    }

    public void pushFloat(float f) {
        smt.push("F");
        if (f == 0) code.addByte(Opcodes.FCONST_0);
        else if (f == 1) code.addByte(Opcodes.FCONST_1);
        else if (f == 2) code.addByte(Opcodes.FCONST_2);
        else {
            code.addByte(Opcodes.LDC);
            code.addByte(cls.getConstPool().addFloat(f));
        }
    }

    public void popToLocal(int index) {
        String type = smt.pop();
        smt.store(index, type);
        switch (type) {
            case "I":
                switch (index) {
                    case 0: code.addByte(Opcodes.ISTORE_0); return;
                    case 1: code.addByte(Opcodes.ISTORE_1); return;
                    case 2: code.addByte(Opcodes.ISTORE_2); return;
                    case 3: code.addByte(Opcodes.ISTORE_3); return;
                    default:
                        code.addByte(Opcodes.ISTORE);
                        code.addByte(index);
                }
            case "F":
                switch (index) {
                    case 0: code.addByte(Opcodes.FSTORE_0); return;
                    case 1: code.addByte(Opcodes.FSTORE_1); return;
                    case 2: code.addByte(Opcodes.FSTORE_2); return;
                    case 3: code.addByte(Opcodes.FSTORE_3); return;
                    default:
                        code.addByte(Opcodes.FSTORE);
                        code.addByte(index);
                }
        }
    }

    public void pushLocal(int index) {
        String type = smt.load(index);
        smt.push(type);
        switch (type) {
            case "I":
                switch (index) {
                    case 0: code.addByte(Opcodes.ILOAD_0); return;
                    case 1: code.addByte(Opcodes.ILOAD_1); return;
                    case 2: code.addByte(Opcodes.ILOAD_2); return;
                    case 3: code.addByte(Opcodes.ILOAD_3); return;
                    default:
                        code.addByte(Opcodes.ILOAD);
                        code.addByte(index);
                }
            case "F":
                switch (index) {
                    case 0: code.addByte(Opcodes.FLOAD_0); return;
                    case 1: code.addByte(Opcodes.FLOAD_1); return;
                    case 2: code.addByte(Opcodes.FLOAD_2); return;
                    case 3: code.addByte(Opcodes.FLOAD_3); return;
                    default:
                        code.addByte(Opcodes.FLOAD);
                        code.addByte(index);
                }
        }
    }

    public void unaryOperator(byte opcode) { code.addByte(opcode); }

    public void binaryOperator(byte opcode) {
        code.addByte(opcode);
        smt.pop();
        smt.push(smt.pop()); // fixme(?)-type check
    }

    public void binaryJmpOperator(byte opcode, Frame target) {
//        smt.useFrame(target);
        smt.pop(2);
        Frame here = assignFrame(new Frame(), false); // only for testing conditional code
        code.addByte(opcode);
        frames.putIfAbsent(target, new ArrayList<>());
        frames.get(target).add(Pair.of(code.size(), here));
        code.addShort(0x0000);
    }
    public void unaryJmpOperator(byte opcode, Frame target) {
//        smt.useFrame(target);
        smt.pop();
        Frame here = assignFrame(new Frame(), false);
        code.addByte(opcode);
        frames.putIfAbsent(target, new ArrayList<>());
        frames.get(target).add(Pair.of(code.size(), here));
        code.addShort(0x0000);
    }
    public void jmpOperator(byte opcode, Frame target) {
//        smt.useFrame(target);
        Frame here = assignFrame(new Frame(), false);
        code.addByte(opcode);
        frames.putIfAbsent(target, new ArrayList<>());
        frames.get(target).add(Pair.of(code.size(), here));
        code.addShort(0x0000);
    }

    public void pop() {
        code.addByte(POP);
        smt.pop();
    }

    public void chop(int amt) { smt.chop(amt); }

    public Frame assignFrame(Frame frame) { return assignFrame(frame, true); }
    private Frame assignFrame(Frame frame, boolean use) {
        if (frame.getOffset() != -1) throw new RuntimeException(); // fixme-maybe not?
        frame.setOffset(code.size());
        smt.setFrame(frame);
        if (use) smt.useFrame(frame);
        return frame;
    }

    @Override
    public ByteList toByteList() {
        code.addByte(Opcodes.RETURN);

        if (smt.getSize() != 0) attributes.add(smt);
        for (Frame frame : frames.keySet())
            for (Pair<Integer, Frame> pair : frames.get(frame)) {
                if (pair.getValue() == null ||
                        !frame.getStack().equals(pair.getValue().getStack()) ||
                        !frame.getLocals().equals(pair.getValue().getLocals())) {
//                    System.out.println(pair.getValue());
//                    throw new RuntimeException("conditional code alters frame");
                }

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
