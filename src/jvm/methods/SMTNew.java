package jvm.methods;

import jvm.AttributeInfo;
import jvm.Opcodes;
import jvm.classes.ClassFile;
import util.ByteList;

import java.util.*;

public class SMTNew extends AttributeInfo implements Opcodes
{
    public static final byte SAME = 0; // to 63
    public static final byte SAME_LOCALS_1_STACK_ITEM = 64; // to 127
    public static final byte SAME_LOCALS_1_STACK_ITEM_EXTENDED = (byte) 247;
    public static final byte CHOP = (byte) 248; // to 250
    public static final byte SAME_FRAME_EXTENDED = (byte) 251;
    public static final byte APPEND = (byte) 252; // to 254
    public static final byte FULL_FRAME = (byte) 255;

    public static final byte ITEM_Top = 0;
    public static final byte ITEM_Integer = 1;
    public static final byte ITEM_Float = 2;
    public static final byte ITEM_Long = 4;
    public static final byte ITEM_Double = 3;
    public static final byte ITEM_Null = 5;
    public static final byte ITEM_UninitializedThis = 6;
    public static final byte ITEM_Object = 7;
    public static final byte ITEM_Uninitialized = 8;

    private static final String POP = "--pop";

    private Block current = new Block(-1), first = current;
    private Frame frame = new Frame(), firstFrame = new Frame();
    private short maxStack, maxLocals, nStack = 0;
    private Set<Label> targets = new HashSet<>();
    public SMTNew(ClassFile cls, String[] params) {
        super(cls, "StackMapTable");
        Collections.addAll(frame.getLocals(), params);
        Collections.addAll(firstFrame.getLocals(), params);
        maxLocals = (short) params.length;
    }

    public void push(String type) {
        if (++nStack > maxStack) maxStack = nStack;
        current.out.getStack().push(type);
        frame.getStack().push(type);
    }
    public String pop() {
        nStack--;
        if (!current.out.getStack().isEmpty()) current.out.getStack().pop();
        return frame.getStack().pop();
    }
    public void pop(int n) {
        nStack -= n;
        for (int i = 0; i < n; i++) {
            if (!current.out.getStack().isEmpty()) current.out.getStack().pop();
            frame.getStack().pop();
        }
    }

    public void store(int index) {
        if (index + 1 > maxLocals) maxLocals = (short) (index + 1);
        int nLocals = current.out.getLocals().size();
        if (nLocals > index) current.out.getLocals().set(index, pop());
        else {
            for (int i = 0; i < index - nLocals; i++) current.out.getLocals().add(null);
            current.out.getLocals().add(pop());
        }
        nLocals = frame.getLocals().size();
        if (nLocals > index) frame.getLocals().set(index, pop());
        else {
            for (int i = 0; i < index - nLocals; i++) frame.getLocals().add(null);
            frame.getLocals().add(pop());
        }
    }

    public String peek() { return frame.getStack().isEmpty() ? null : frame.getStack().peek(); }
    public String localType(int index) { return frame.getLocals().get(index); }

    public int getSize() { return targets.size(); }
    public short getMaxStack() { return maxStack; }
    public short getMaxLocals() { return maxLocals; }

    public void jmp(Label target, int currentIndex) {
        current.next.add(target);
        targets.add(target);
        assign(new Label(), currentIndex);
    }
    public void assign(Label label, int currentIndex) {
        label.block = new Block(currentIndex);
        current.next.add(label);
        current = label.block;
    }

    static class Block {
        private int offset;
        private Frame in = new Frame(), out = new Frame();
        private Set<Label> next = new HashSet<>();
        public Block(int offset) { this.offset = offset; }
        public int getOffset() { return offset; }
    }

    private void updateFrames() {
        Set<Block> changed = new HashSet<>();
        changed.add(first);
        Frame in = firstFrame;
        // todo next- figure out how to do this loop on the go
        while (!changed.isEmpty()) {
            Set<Block> next = new HashSet<>();
            for (Block b : changed) {
                Frame m = merge(b.in, in);
                if (m.equals(b.in)) continue;
                b.in = m;
                in = merge(b.in, b.out);
                b.next.forEach(l -> next.add(l.block));
            }
            changed = next;
        }
    }

    @Override
    public ByteList toByteList() {
        info.addShort(targets.size());

        updateFrames();

        Block block = first;
        Set<Label> labels = new TreeSet<>();
        labels.addAll(targets);
        System.out.println(block.in.getLocals());
        for (Label l : labels) {
            Frame frame = l.block.in, prev = block.in;
            System.out.println(frame.getLocals());
            int offsetDelta = l.block.offset - block.offset - 1;

            if (frame.getStack().empty() && frame.getLocals().equals(prev.getLocals())) {
                if (offsetDelta < 64) info.addByte(offsetDelta + SAME);
                else {
                    info.addByte(SAME_FRAME_EXTENDED);
                    info.addShort(offsetDelta);
                }
            }
            else if (frame.getStack().size() == 1 && frame.getLocals().equals(prev.getLocals())) {
                if (offsetDelta < 64) info.addByte(offsetDelta + SAME_LOCALS_1_STACK_ITEM);
                else {
                    info.addByte(SAME_LOCALS_1_STACK_ITEM_EXTENDED);
                    info.addShort(offsetDelta);
                }
                addVarInfo(frame.getStack().peek());
            }
            else {
                int prevSize = prev.getLocals().size(), diff = frame.getLocals().size() - prevSize;
                if (frame.getStack().empty() && diff < 3 && diff > -3) {
                    info.addByte(251 + diff); // takes care of both chop & append frames
                    info.addShort(offsetDelta);
                    for (int t = 0; t < diff; t++) addVarInfo(frame.getLocals().get(t + prevSize));
                }
                else {
                    info.addByte(FULL_FRAME);
                    info.addShort(offsetDelta);
                    info.addShort(frame.getLocals().size());
                    frame.getLocals().forEach(this::addVarInfo);
                    info.addShort(frame.getStack().size());
                    frame.getStack().forEach(this::addVarInfo);
                }
            }
            block = l.block;
        }

        return super.toByteList();
    }

    private Frame merge(Frame a, Frame b) {
        Frame res = new Frame();
        int al = a.getLocals().size(), bl = b.getLocals().size();
        for (int i = 0; i < bl; i++)
            if (b.getLocals().get(i) == null && al > i) res.getLocals().add(a.getLocals().get(i));
            else res.getLocals().add(b.getLocals().get(i));
        for (int i = bl; i < al; i++)
            res.getLocals().add(a.getLocals().get(i));

        // how do you merge stacks?
//        res.getStack().addAll(a.getStack());
        res.getStack().addAll(b.getStack()); // fixme this IS p̶r̶o̶b̶a̶b̶l̶y̶ incorrect, look into: github.com/llbit/ow2-asm/blob/a695934043d6f8f0aee3f6867c8dd167afd4aed8/src/org/objectweb/asm/Frame.java
        return res;
    }

    private void addVarInfo(String type) {
        if (type == null) return; // handle items that take 2 locations
        if (type.startsWith("[")) {
            info.addByte(ITEM_Object);
            info.addShort(cls.getConstPool().addClass(type));
        } else switch (type) {
            case "I": info.addByte(ITEM_Integer); break;
            case "F": info.addByte(ITEM_Float); break;
            case "J": info.addByte(ITEM_Long); break;
            case "D": info.addByte(ITEM_Double); break;
            default: // fixme handle other ITEMs
                info.addByte(ITEM_Object);
                info.addShort(cls.getConstPool().addClass(type.substring(1, type.length() - 1)));
        }
    }
}
