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
    protected String name; // only needed for debugging purposes
    boolean used = false;

    public Symbol(String name) { this.name = name; }

    public final Symbol addProduction(Production.ASTGenerator generator, Symbol... p) {
        if (used) throw new IllegalStateException("Cannot Modify Symbol " + name + " After Constructing Grammar!");
        productions.add(new Production(generator, this, p));
        return this;
    }
    public Set<Production> getProductions() { return productions; }

    public boolean isTerminal() { return false; }

    @Override
    public String toString() { return name; }
}
