package parsing.slr;

import lang.Production;
import lang.Symbol;

import java.util.Arrays;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/9/2017.
 */
class Item
{
    Production production;
    int index;

    Item(Production production, Integer index) {
        this.production = production;
        this.index = index;
    }
    Item next() { return new Item(production, index + 1); } // does not check for index out of bound

    boolean canReduce() { return index == production.getValue().length; }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(production.getKey()).append("-> ");
        for (int i = 0; i < production.getValue().length; i++) {
            if (i == index) s.append('.');
            s.append(production.getValue()[i]);
        } if (canReduce()) s.append('.');
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Item &&
                ((Item) o).production.equals(production) &&
                ((Item) o).index == index;
    }
    @Override
    public int hashCode() {
        int result = production != null ? production.hashCode() : 0;
        result = 31 * result + index;
        return result;
    }
}
