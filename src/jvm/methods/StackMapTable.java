package jvm.methods;

import jvm.AttributeInfo;
import jvm.classes.ClassFile;
import util.ByteList;

import java.util.*;

public class StackMapTable extends AttributeInfo
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

    private short stackSize = 0, maxStack, maxLocals;
    private Set<Frame> frames = new TreeSet<>(); // only used frames
    private Frame first = new Frame();
    private Frame frame = first;

    StackMapTable(ClassFile cls, String[] params) {
        super(cls, "StackMapTable");
        first.setOffset(-1);
        for (int i = 0; i < params.length; i++) store(i, params[i]);
        frame = new Frame().assign(first);
    }

    void setFrame(Frame frame) { this.frame = frame.assign(this.frame); }
    void useFrame() {
        frames.add(frame);
        frame = new Frame().assign(frame);
    }

    void push(String type) {
        if (isDouble(type)) push(null);
        frame.getStack().push(type);
        if (maxStack == stackSize++) maxStack++;
    }
    String pop() {
        stackSize--;
        String type = frame.getStack().pop();
        if (isDouble(type)) pop(); // type takes up 2 locations
        return type;
    }
    void pop(int amt) {
        stackSize -= amt;
        for (int i = 0; i < amt; i++)
            if (isDouble(frame.getStack().pop()))
                frame.getStack().pop(); // type takes up 2 locations
    }

    void store(int index, String type) {
        if (index + 1 > maxLocals) maxLocals = (short) (index + 1);
        if (index == frame.getLocals().size()) frame.getLocals().add(type);
        else if (index < frame.getLocals().size()) frame.getLocals().set(index, type);
        else throw new RuntimeException("locals array index out of bounds");
        if (isDouble(type)) store(index + 1, null);
    }
    String load(int index) { return frame.getLocals().get(index); }
    void chop(int amt) {
        for (int i = 0; i < amt; i++)
            frame.getLocals().remove(frame.getLocals().size() - 1);
    }

    short getMaxStack() { return maxStack; }
    short getMaxLocals() { return maxLocals; }

    int getSize() { return frames.size(); }

    private static boolean isDouble(String type) { return type != null && (type.equals("J") || type.equals("D")); }

    private void addVarInfo(String type) {
        if (type == null) return; // handle items that take 2 locations
        switch (type) {
            case "I": info.addByte(ITEM_Integer); break;
            case "F": info.addByte(ITEM_Float); break;
            case "J": info.addByte(ITEM_Long); break;
            case "D": info.addByte(ITEM_Double); break;
            default: // fixme handle other ITEMs
                info.addByte(ITEM_Object);
                info.addShort(cls.getConstPool().addClass(type.substring(2, type.length() - 1)));
        }
    }

    public String peekStack() { return frame.getStack().peek(); }

    @Override
    public ByteList toByteList() {
        info.addShort(frames.size()); // number_of_entries
        Frame prev = first;
        for (Frame frame : frames) {  // entries[number_of_entries]
            int offsetDelta = frame.getOffset() - prev.getOffset() - 1;

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
            prev = frame;
        }
        return super.toByteList();
    }
}
