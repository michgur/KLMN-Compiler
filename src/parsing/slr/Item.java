package parsing.slr;

import parsing.Symbol;

import java.util.Arrays;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/9/2017.
 */
class Item
{
    Symbol symbol;
    Symbol[] production;
    int index;

    Item(Symbol symbol, Symbol[] production, Integer index) {
        this.symbol = symbol;
        this.production = production;
        this.index = index;
    }
    Item next() { return new Item(symbol, production, index + 1); } // does not check for index out of bound

    boolean canReduce() { return index == production.length; }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(symbol).append("-> ");
        for (int i = 0; i < production.length; i++) {
            if (i == index) s.append('.');
            s.append(production[i]);
        } if (canReduce()) s.append('.');
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Item &&
                ((Item) o).symbol == symbol &&
                ((Item) o).index == index &&
                Arrays.equals(production, ((Item) o).production);
    }
    @Override
    public int hashCode() {
        int result = symbol != null ? symbol.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(production);
        result = 31 * result + index;
        return result;
    }
}
