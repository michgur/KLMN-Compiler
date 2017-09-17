package test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * ಠ^ಠ.
 * Created by Michael on 9/17/2017.
 */
public class SymbolTable
{
    private Deque<Map<String, Integer>> table = new ArrayDeque<>();
    private int index = 0;

    public void addSymbol(String symbol) { table.peek().put(symbol, index++); }
    public Integer findSymbol(String symbol) {
        for (Map<String, Integer> scope : table)
            if (scope.containsKey(symbol)) return scope.get(symbol);
        throw new RuntimeException("symbol " + symbol + " not defined!");
    }
    public boolean checkScope(String symbol) { return table.peek().containsKey(symbol); }

    public void enterScope() { table.push(new HashMap<>()); }
    public void exitScope() { table.pop(); }
}
