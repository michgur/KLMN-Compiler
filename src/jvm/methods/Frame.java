package jvm.methods;

import java.util.*;

class Frame implements Comparable
{
    private Stack<String> stack = new Stack<>();
    private List<String> locals = new ArrayList<>();

    private int offset = -1;
    private boolean assigned = false;

    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = offset; }

    Frame assign(Frame prev) {
        stack.addAll(prev.stack);
        locals.addAll(prev.locals);
        assigned = true;
        return this;
    }

    Stack<String> getStack() { return stack; }
    List<String> getLocals() { return locals; }
    boolean isAssigned() { return assigned; }

    @Override
    public int compareTo(Object o) { return Integer.compare(offset, ((Frame) o).offset); }

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
    public String toString() { return "Frame { locals: " + locals + ", stack: " + stack + '}'; }
}
