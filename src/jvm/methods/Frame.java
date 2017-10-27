package jvm.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Frame implements Comparable
{
    private Stack<String> stack = new Stack<>();
    private List<String> locals = new ArrayList<>();

    private int offset = -1;
    private int hash = new Random().nextInt();

    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = offset; }

    Frame assign(Frame prev) {
        stack.addAll(prev.stack);
        locals.addAll(prev.locals);
        return this;
    }

    Stack<String> getStack() { return stack; }
    List<String> getLocals() { return locals; }

    @Override
    public int hashCode() { return hash; }

    @Override
    public int compareTo(Object o) { return Integer.compare(offset, ((Frame) o).offset); }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Frame)) { System.out.println("poop");return false;}
        Frame f = (Frame) obj;
        if (f.locals.size() != locals.size() || f.stack.size() != stack.size())
        { System.out.println(locals.size() + " " + f.locals.size());return false;}
        for (int i = 0; i < stack.size(); i++) if (!stack.get(i).equals(f.stack.get(i)))
        { System.out.println(locals.get(i) + " " + f.locals.get(i));return false;}
        for (int i = 0; i < locals.size(); i++) if (!locals.get(i).equals(f.locals.get(i)))
        { System.out.println(locals.get(i) + " " + f.locals.get(i));return false;}
        return true;
    }
}
