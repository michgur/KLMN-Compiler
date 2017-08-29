package ast;

import lang.Symbol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/29/2017.
 */
public class ASTFactory
{
    private static Map<List<Symbol>, ASTFactory> factories = new HashMap<>();

    private ASTGenerator generator;
    public ASTFactory(ASTGenerator generator, Symbol... production) {
        factories.put(Arrays.asList(production), this);
        this.generator = generator;
    }

    public AST generate(AST[] children) { return generator.generate(children); }

    public static ASTFactory getFactory(Symbol... production) { return factories.get(Arrays.asList(production)); }

    public interface ASTGenerator { AST generate(AST[] children); }
}
