package jvm.methods;

import jvm.AttributeInfo;
import jvm.JVMType;
import jvm.Opcodes;
import jvm.classes.ClassFile;
import util.ByteList;
import util.Pair;

import java.util.*;

/* This shitty implementation of the mess that is the StackMapTable attribute does not check the validity of the bytecode.
 * It also does not provide any type information. The compiler is expected to handle type information by its own. */
public class StackMapTable extends AttributeInfo implements Opcodes
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

    private static final JVMType POP = JVMType.refType("^"), UNKNOWN = JVMType.refType("?");

    private Block current = new Block(-1), first = current;
    private Frame firstFrame = new Frame();
    private short maxStack, maxLocals, nStack = 0;
    private Set<Label> targets = new HashSet<>();
    
    public StackMapTable(ClassFile cls, JVMType[] params) {
        super(cls, "StackMapTable");
        Collections.addAll(firstFrame.locals, params);
        maxLocals = (short) params.length;
    }

    public void push(JVMType type) {
        if (++nStack > maxStack) maxStack = nStack;
        current.out.stack.push(type);
    }
    public void pop() {
        nStack--;
        if (!current.out.stack.isEmpty()
                && current.out.stack.peek() != UNKNOWN && current.out.stack.peek() != POP)
            current.out.stack.pop();
        else current.out.stack.push(POP);
    }
    public void pop(int n) { for (int i = 0; i < n; i++) pop(); }
    public JVMType peek() { return current.out.stack.peek(); }

    public void store(int index, JVMType type) {
        if (index + 1 > maxLocals) maxLocals = (short) (index + 1);
        int nLocals = current.out.locals.size();
        if (nLocals > index) current.out.locals.set(index, type);
        else {
            for (int i = 0; i < index - nLocals; i++) current.out.locals.add(UNKNOWN);
            current.out.locals.add(type);
        }
    }

    @Override
    public boolean include() { return true; }

    /* Required stack size for the method. Only use after fully writing code. */
    public short getMaxStack() { return maxStack; }
    /* Required local array size for the method. Only use after fully writing code. */
    public short getMaxLocals() { return maxLocals; }

    /* Jump from codePointer to Label */
    public void jmp(Label target, int codePointer, boolean isGOTO) {
        current.next.add(target);
        targets.add(target);
        Label next = new Label();
        assign(next, codePointer);
        if (isGOTO) targets.add(next);
    }
    
    /* Assign a Label at codePointer */
    public void assign(Label label, int codePointer) {
        if (current.offset == codePointer) label.block = current;
        else {
            label.block = new Block(codePointer);
            current.next.add(label);
            current = label.block;
        }
    }

    /* Represents a StackMap Frame */
    private static class Frame
    {
        private final Stack<JVMType> stack = new Stack<>();
        private final List<JVMType> locals = new ArrayList<>();

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Frame)) return false;
            Frame f = (Frame) obj;
            if (f.locals.size() != locals.size() || f.stack.size() != stack.size()) return false;
            for (int i = 0; i < stack.size(); i++) if (!Objects.equals(stack.get(i), f.stack.get(i))) return false;
            for (int i = 0; i < locals.size(); i++) if (!Objects.equals(locals.get(i), f.locals.get(i))) return false;
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("StackMapFrame {\n\tlocals: [");
            locals.forEach(local -> builder.append(local).append(", "));
            builder.append("]\n\tstack: [");
            stack.forEach(item -> builder.append(item).append(", "));
            return builder.append("]\n}").toString();
        }
    }

    /* Represents a basic-block of code */
    static class Block implements Comparable<Block> {
        private static int index = 0;
        /* Code pointer for block start */
        private final int offset, i;
        /* Input and output frames of block */
        private Frame in = new Frame(), out = new Frame();
        // replace next (a set is not required, only two possible branch targets)
        private final Set<Label> next = new HashSet<>();
        /* Whether the Block is reachable from other blocks / is the first block */
        private boolean reachable = false;
        public Block(int offset) {
            i = index++;
            this.offset = offset;
            out.stack.push(UNKNOWN);
            in.stack.push(UNKNOWN);
        }
        public int getOffset() { return offset; }
        /* Used to sort blocks by code location */
        @Override public int compareTo(Block b) { return Integer.compare(offset, b.offset); }
        @Override public String toString() { return "block " + i; }
    }

    private void updateFrames(Block block, Frame input, Set<Pair<Block, Label>> visited) {
        block.reachable = true;
        if (input.equals(block.in)) return;
        block.in = merge(block.in, input); // this merge appears to be redundant
        Frame nextInput = merge(block.in, block.out);
        for (Label label : block.next) {
            if (visited.contains(Pair.of(block, label))) continue;
            visited.add(Pair.of(block, label));
            updateFrames(label.block, nextInput, visited);
        }
    }

    @Override
    public ByteList toByteList() {
        updateFrames(first, firstFrame, new HashSet<>());

        Block prev = first;
        Set<Block> blocks = new TreeSet<>(); // sorted set (Block implements Comparable)
        targets.forEach(t -> blocks.add(t.block));

        info.addShort(blocks.size());
        for (Block b : blocks) {
            if (!b.reachable)
                throw new RuntimeException("unreachable code at @" + b.offset);
            Frame frame = b.in;
            int offsetDelta = b.offset - prev.offset - 1;

            if (frame.stack.empty() && frame.locals.equals(prev.in.locals)) {
                if (offsetDelta < 64) info.addByte(SAME + offsetDelta);
                else {
                    info.addByte(SAME_FRAME_EXTENDED);
                    info.addShort(offsetDelta);
                }
            }
            else if (frame.stack.size() == 1 && frame.locals.equals(prev.in.locals)) {
                if (offsetDelta < 64) info.addByte(SAME_LOCALS_1_STACK_ITEM + offsetDelta);
                else {
                    info.addByte(SAME_LOCALS_1_STACK_ITEM_EXTENDED);
                    info.addShort(offsetDelta);
                }
                addVarInfo(frame.stack.peek());
            }
            else {
                int prevSize = prev.in.locals.size(), diff = frame.locals.size() - prevSize;
                if (frame.stack.empty() && diff < 3 && diff > -3) {
                    info.addByte(251 + diff); // takes care of both chop & append frames
                    info.addShort(offsetDelta);
                    for (int t = 0; t < diff; t++) addVarInfo(frame.locals.get(t + prevSize));
                }
                else {
                    info.addByte(FULL_FRAME);
                    info.addShort(offsetDelta);
                    info.addShort(frame.locals.size());
                    frame.locals.forEach(this::addVarInfo);
                    info.addShort(frame.stack.size());
                    frame.stack.forEach(this::addVarInfo);
                }
            }
            prev = b;
        }

        return super.toByteList();
    }

    private Frame merge(Frame a, Frame b) {
        Frame res = new Frame();
        int al = a.locals.size(), bl = b.locals.size();
        // only use locals information of a when there is none in b
        for (int i = 0; i < bl; i++)
            if (b.locals.get(i) == UNKNOWN && al > i) res.locals.add(a.locals.get(i));
            else res.locals.add(b.locals.get(i));
        if (bl == 0) for (int i = 0; i < al; i++)
            res.locals.add(a.locals.get(i));

        if (!b.stack.empty() && b.stack.get(0) == UNKNOWN)
            res.stack.addAll(a.stack);
        b.stack.forEach(i -> {
            if (i == UNKNOWN) return;
            if (i == POP) res.stack.pop();
            else res.stack.push(i);
        });
        return res;
    }

    private void addVarInfo(JVMType type) {
        if (type == JVMType.INTEGER) info.addByte(ITEM_Integer);
        else if (type == JVMType.FLOAT) info.addByte(ITEM_Float);
        else if (type == JVMType.LONG) {
            info.addByte(ITEM_Long);
            info.addByte(ITEM_Top);
        }
        else if (type == JVMType.DOUBLE) {
            info.addByte(ITEM_Double);
            info.addByte(ITEM_Top);
        }
        else {
            info.addByte(ITEM_Object);
            info.addShort(cls.getConstPool().addClass(type));
        }
    }
}
