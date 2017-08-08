package parsing;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/8/2017.
 */
public class Item
{
    private Symbol symbol;
    private Symbol[] production;
    private int index;

    public Item(Symbol symbol, Symbol[] production, int index) {
        this.symbol = symbol;
        this.production = production;
        this.index = index;
    }

    public Item nextItem() { return (index + 1 < production.length) ? new Item(symbol, production, index + 1) : null; }

    public Symbol getSymbol() { return symbol; }
    public Symbol[] getProduction() { return production; }
    public int getIndex() { return index; }
}
