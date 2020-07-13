package codegen;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import lexing.Symbol;
import parsing.AST;

import java.util.HashMap;
import java.util.Map;

public class CodeGenerator
{ //TODO come up with a better name
    public interface GeneratorNew { void apply(MethodVisitor mv, AST[] asts); }
    private Map<Symbol, GeneratorNew> generators = new HashMap<>();

    public void addGenerator(Symbol symbol, GeneratorNew generator) { generators.put(symbol, generator); }

    // fixme AST nodes are represented by Tokens, when they should be represented by Symbols, and have an additional field for the actual string
    public void apply(MethodVisitor mv, AST ast) { generators.get(ast.getValue()).apply(mv, ast.getChildren()); }
}
