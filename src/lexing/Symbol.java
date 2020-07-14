package lexing;

import java.util.HashSet;
import java.util.Set;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/4/2017.
 */
public class Symbol
{
    public static final Symbol EPSILON = new Symbol("ε");

    private Set<Symbol[]> productions = new HashSet<>();
    private String name; // only needed for debugging purposes
    boolean modifiable = true; // changed when Symbol is passed to Grammar

    public Symbol(String name) { this.name = name; }

    public final void addProduction(Symbol... p) {
        if (!modifiable) throw new IllegalStateException("Cannot Modify Symbol " + name + " After Constructing Grammar!");
        productions.add(p);
    }

    public Set<Symbol[]> getProductions() { return productions; }

    public boolean isTerminal() { return false; }

    public String getName() { return name; }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return name != null ? name.equals(symbol.name) : symbol.name == null;
    }

    @Override
    public int hashCode() { return name != null ? name.hashCode() : 0; }
}
