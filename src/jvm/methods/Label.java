package jvm.methods;

public class Label implements Comparable<Label>
{
    StackMapTable.Block block;
    private boolean wide = false;

    @Override
    public int compareTo(Label label) {
        return Integer.compare(block.getOffset(),
                label.block.getOffset());
    }

    public boolean isWide() { return wide; }
    public void setWide(boolean wide) { this.wide = wide; }
}
