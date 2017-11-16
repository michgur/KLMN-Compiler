package jvm.methods;

public class Label implements Comparable
{
    SMTNew.Block block;
    @Override
    public int compareTo(Object o) {
        return Integer.compare(block.getOffset(),
                ((Label) o).block.getOffset());
    }
}
