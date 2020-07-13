package codegen;

import java.util.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 9/17/2017.
 */
public class SymbolTable
{
    private Deque<Map<String, Integer>> table = new ArrayDeque<>();
    private int index = 1; // args[] is at index 0

    public void addSymbol(String symbol) { table.peek().put(symbol, index++); }
    public Integer findSymbol(String symbol) {
        for (Map<String, Integer> scope : table)
            if (scope.containsKey(symbol)) return scope.get(symbol);
        throw new RuntimeException("symbol " + symbol + " not defined!");
    }
    public boolean checkScope(String symbol) { return table.peek().containsKey(symbol); }

    public void enterScope() { table.push(new HashMap<>()); }
    public int exitScope() {
        int size = table.pop().size();
        index -= size;
        return size;
    }
}
