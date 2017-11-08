package klmn.writing;

import util.Pair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable
{
    private Deque<Scope> st = new ArrayDeque<>();
    private int index = 0;

    public void addSymbol(String symbol, int type) { st.peek().getKey().put(symbol, Pair.of(index++, type)); }
    public int findSymbol(String symbol) {
        for (Scope scope : st)
            if (scope.getKey().containsKey(symbol)) return scope.getKey().get(symbol).getKey();
        throw new RuntimeException("symbol " + symbol + " not defined!");
    }
    public int typeOf(String symbol) {
        for (Scope scope : st)
            if (scope.getKey().containsKey(symbol)) return scope.getKey().get(symbol).getValue();
        throw new RuntimeException("symbol " + symbol + " not defined!");
    }
    public ScopeType scopeTypeOf(String symbol) {
        for (Scope scope : st)
            if (scope.getKey().containsKey(symbol)) return scope.getValue();
        throw new RuntimeException("symbol " + symbol + " not defined!");
    }
    public boolean checkScope(String symbol) { return st.peek().getKey().containsKey(symbol); }

    public void enterScope(ScopeType type) { st.push(new Scope(type)); }
    public int exitScope() {
        int size = st.pop().getKey().size();
        index -= size;
        return size;
    }

    public enum ScopeType { MODULE, CLASS, FUNCTION, BLOCK }

    private static class Scope extends Pair<Map<String, Pair<Integer, Integer>>, ScopeType>
    { private Scope(ScopeType type) { super(new HashMap<>(), type); } }
}
