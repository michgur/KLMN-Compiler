package ast;

import lang.Symbol;
import util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ಠ^ಠ.
 * Created by Michael on 9/12/2017.
 */
public class ASTFactory
{
    private Map<Production, Generator> generators = new HashMap<>();
    public interface Generator { AST generate(AST[] children); }

    public void addProduction(Symbol k, Symbol[] v, Generator generator) { generators.put(new Production(k, v), generator); }
    public AST generate(Symbol k, Symbol[] v, AST[] children) { return generators.get(new Production(k, v)).generate(children); }

    private static class Production extends Pair<Symbol, Symbol[]>
    {
        private Production(Symbol key, Symbol[] value) { super(key, value); }
        @Override
        public boolean equals(Object o) {
            return o instanceof Production && getKey().equals(((Production) o).getKey()) &&
                    Arrays.equals(getValue(), ((Production) o).getValue());
        }
        @Override
        public int hashCode() { return 31 * getKey().hashCode() + Arrays.hashCode(getValue()); }
        @Override public String toString() { return getKey() + " -> " + Arrays.toString(getValue()); }
    }
}
