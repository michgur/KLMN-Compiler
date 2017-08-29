package parsing;

import lang.Symbol;

import java.util.Arrays;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/9/2017.
 */
class Item
{
    Symbol key;
    Symbol[] value;
    int index;

    Item(Symbol symbol, Symbol[] production, Integer index) {
        key = symbol;
        value = production;
        this.index = index;
    }
    Item next() { return new Item(key, value, index + 1); } // does not check for index out of bound

    boolean canReduce() { return index == value.length; }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(key).append("-> ");
        for (int i = 0; i < value.length; i++) {
            if (i == index) s.append('.');
            s.append(value[i]);
        } if (canReduce()) s.append('.');
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Item &&
                ((Item) o).key.equals(key) &&
                Arrays.equals(((Item) o).value, value) &&
                ((Item) o).index == index;
    }
    @Override
    public int hashCode() {
        return 31 * (31 * (key != null ? key.hashCode() : 0)
                + Arrays.hashCode(value)) + index;
    }
}
