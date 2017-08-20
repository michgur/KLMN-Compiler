package lang;

import ast.AST;
import javafx.util.Pair;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/15/2017.
 */
public class Production extends Pair<Symbol, Symbol[]>
{
    private ASTGenerator generator;
    public Production(ASTGenerator generator, Symbol key, Symbol... value) {
        super(key, value);
        this.generator = generator;
    }

    public AST generateAST(AST[] children) { return generator.generate(children); }

    public interface ASTGenerator { AST generate(AST[] children); }
}
