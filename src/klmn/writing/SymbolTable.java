package klmn.writing;

import util.Pair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static klmn.writing.TypeEnv.Type;

public class SymbolTable
{
    private Deque<Scope> st = new ArrayDeque<>();

    public void addSymbol(String symbol, Type type) {
        if (st.peek().getKey().containsKey(symbol)) throw new RuntimeException("symbol " + symbol + " already defined!");
        st.peek().getKey().put(symbol, Pair.of(st.peek().index++, type));
    }
    public int findSymbol(String symbol) {
        for (Scope scope : st)
            if (scope.getKey().containsKey(symbol)) return scope.getKey().get(symbol).getKey();
        throw new RuntimeException("symbol " + symbol + " not defined!");
    }
    public Type typeOf(String symbol) {
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

    public void enterScope(ScopeType type)
    { st.push(new Scope((type == ScopeType.FUNCTION || st.isEmpty()) ? 0 : st.peek().index, type)); }
    public int exitScope() { return st.pop().getKey().size(); }

    public ScopeType getScopeType() { return st.peek().getValue(); }

    public enum ScopeType { MODULE, CLASS, FUNCTION, BLOCK }

    public void ret() { st.peek().ret = true; }
    public boolean hasRet() { return st.peek().ret; }

    private static class Scope extends Pair<Map<String, Pair<Integer, Type>>, ScopeType> {
        private int index;
        private boolean ret = false;
        private Scope(int index, ScopeType type) {
            super(new HashMap<>(), type);
            this.index = index;
        }
    }
}
