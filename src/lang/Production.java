package lang;

import ast.AST;
import javafx.util.Pair;
import parsing.ParseTree;

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

    public AST generateAST(ParseTree tree) { return generator.generate(tree); }

    // eventually, this will be called with Tokens, not ParseTrees,
    // since we're skipping CST generation
    public interface ASTGenerator { AST generate(ParseTree tree); }
}
