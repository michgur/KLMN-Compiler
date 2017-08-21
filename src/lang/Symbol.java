package lang;

import java.util.HashSet;
import java.util.Set;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/4/2017.
 */
public class Symbol
{
    public static final Symbol EPSILON = new Symbol("ε");

    private Set<Production> productions = new HashSet<>();
    boolean modifiable = true;
    private String name; // only needed for debugging purposes

    public Symbol(String name) { this.name = name; }

    public final void addProduction(Symbol... p) { addProduction(c -> c[0], p); } // not sure yet if this is a great idea
    public final void addProduction(Production.ASTGenerator generator, Symbol... p) {
        if (!modifiable) throw new IllegalStateException("Cannot Modify Symbol " + name + " After Constructing Grammar!");
        productions.add(new Production(generator, this, p));
    }

    public Set<Production> getProductions() { return productions; }

    public boolean isTerminal() { return false; }

    @Override
    public String toString() { return name; }
}
